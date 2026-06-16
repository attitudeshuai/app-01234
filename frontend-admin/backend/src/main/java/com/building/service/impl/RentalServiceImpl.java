package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.common.Constants;
import com.building.dto.ContractDTO;
import com.building.dto.RentalDTO;
import com.building.entity.Building;
import com.building.entity.Rental;
import com.building.entity.RentalContract;
import com.building.entity.Room;
import com.building.mapper.RentalContractMapper;
import com.building.mapper.RentalMapper;
import com.building.service.BuildingService;
import com.building.service.RentalService;
import com.building.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class RentalServiceImpl extends ServiceImpl<RentalMapper, Rental> implements RentalService {

    @Autowired
    private RentalContractMapper contractMapper;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService;

    @Override
    public Page<Rental> pageList(int current, int size, String tenantName, String status) {
        LambdaQueryWrapper<Rental> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tenantName)) {
            wrapper.like(Rental::getTenantName, tenantName);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Rental::getStatus, status);
        }
        wrapper.orderByDesc(Rental::getCreatedAt);

        Page<Rental> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(r -> {
            Room room = roomService.getById(r.getRoomId());
            if (room != null) {
                r.setRoomNumber(room.getRoomNumber());
                Building building = buildingService.getById(room.getBuildingId());
                r.setBuildingName(building != null ? building.getName() : "");
            }
        });
        return page;
    }

    @Override
    public void createRental(RentalDTO dto) {
        // 业务校验：检查房屋是否存在
        Room room = roomService.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 业务校验：检查房屋状态是否为空闲
        if (!Constants.ROOM_STATUS_FREE.equals(room.getStatus())) {
            throw new BusinessException("该房屋当前状态为【" + room.getStatus() + "】，不可出租");
        }

        // 业务校验：日期范围
        if (dto.getEndDate() != null && dto.getStartDate() != null) {
            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new BusinessException("结束日期必须晚于起始日期");
            }
        }

        Rental rental = new Rental();
        BeanUtils.copyProperties(dto, rental);
        rental.setStatus(Constants.RENTAL_ACTIVE);
        save(rental);

        // 更新房屋状态为出租
        room.setStatus(Constants.ROOM_STATUS_RENTED);
        roomService.updateById(room);

        log.info("新增租赁: 租户={}, 房间ID={}", rental.getTenantName(), rental.getRoomId());
    }

    @Override
    public void updateRental(RentalDTO dto) {
        Rental rental = getById(dto.getId());
        if (rental == null) {
            throw new BusinessException("租赁记录不存在");
        }

        // 业务校验：日期范围
        if (dto.getEndDate() != null && dto.getStartDate() != null) {
            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new BusinessException("结束日期必须晚于起始日期");
            }
        }

        BeanUtils.copyProperties(dto, rental);
        updateById(rental);
        log.info("修改租赁: ID={}", rental.getId());
    }

    @Override
    public void terminateRental(Long id) {
        Rental rental = getById(id);
        if (rental == null) {
            throw new BusinessException("租赁记录不存在");
        }
        rental.setStatus(Constants.RENTAL_TERMINATED);
        updateById(rental);

        // 更新房屋状态为空闲
        Room room = roomService.getById(rental.getRoomId());
        if (room != null) {
            room.setStatus(Constants.ROOM_STATUS_FREE);
            roomService.updateById(room);
        }

        // 终止关联合约
        List<RentalContract> contracts = contractMapper.selectList(
                new LambdaQueryWrapper<RentalContract>()
                        .eq(RentalContract::getRentalId, id)
                        .eq(RentalContract::getStatus, Constants.CONTRACT_ACTIVE));
        contracts.forEach(c -> {
            c.setStatus(Constants.CONTRACT_TERMINATED);
            contractMapper.updateById(c);
        });

        log.info("终止租赁: ID={}", id);
    }

    @Override
    public List<RentalContract> getContracts(Long rentalId) {
        return contractMapper.selectList(
                new LambdaQueryWrapper<RentalContract>()
                        .eq(RentalContract::getRentalId, rentalId)
                        .orderByDesc(RentalContract::getSignDate));
    }

    @Override
    public List<RentalContract> getAllContracts() {
        List<RentalContract> contracts = contractMapper.selectList(
                new LambdaQueryWrapper<RentalContract>()
                        .orderByDesc(RentalContract::getSignDate));
        // 填充租赁方信息
        contracts.forEach(c -> {
            Rental rental = getById(c.getRentalId());
            if (rental != null) {
                c.setTenantName(rental.getTenantName());
                Room room = roomService.getById(rental.getRoomId());
                if (room != null) {
                    c.setRoomNumber(room.getRoomNumber());
                }
            }
        });
        return contracts;
    }

    @Override
    public void createContract(Long rentalId, ContractDTO dto) {
        Rental rental = getById(rentalId);
        if (rental == null) {
            throw new BusinessException("租赁记录不存在");
        }

        // 业务校验：检查租赁状态
        if (!Constants.RENTAL_ACTIVE.equals(rental.getStatus())) {
            throw new BusinessException("租赁记录状态为【" + rental.getStatus() + "】，无法新增合约");
        }

        // 业务校验：日期范围
        if (dto.getEndDate() != null && dto.getStartDate() != null) {
            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new BusinessException("合同终止日期必须晚于起始日期");
            }
        }

        // 检查合同编号唯一
        long count = contractMapper.selectCount(
                new LambdaQueryWrapper<RentalContract>()
                        .eq(RentalContract::getContractNumber, dto.getContractNumber()));
        if (count > 0) {
            throw new BusinessException("合同编号已存在");
        }

        RentalContract contract = new RentalContract();
        BeanUtils.copyProperties(dto, contract);
        contract.setRentalId(rentalId);
        contract.setStatus(Constants.CONTRACT_ACTIVE);
        contractMapper.insert(contract);
        log.info("新增合约: {} (租赁ID: {})", contract.getContractNumber(), rentalId);
    }
}
