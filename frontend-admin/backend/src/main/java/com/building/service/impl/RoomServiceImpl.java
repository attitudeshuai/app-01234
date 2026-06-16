package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.dto.RoomDTO;
import com.building.entity.Building;
import com.building.entity.Room;
import com.building.mapper.RoomMapper;
import com.building.security.UserContext;
import com.building.service.BuildingService;
import com.building.service.RoomService;
import com.building.service.SysDepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Autowired
    private SysDepartmentService departmentService;

    @Autowired
    private BuildingService buildingService;

    @Override
    public Page<Room> pageList(int current, int size, Long buildingId, Long departmentId,
                               String roomNumber, String purpose, String status) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();

        // 数据权限过滤
        UserContext ctx = UserContext.get();
        if (ctx != null && !ctx.isAdmin()) {
            wrapper.eq(Room::getDepartmentId, ctx.getDepartmentId());
        }

        if (buildingId != null) {
            wrapper.eq(Room::getBuildingId, buildingId);
        }
        if (departmentId != null) {
            wrapper.eq(Room::getDepartmentId, departmentId);
        }
        if (StringUtils.hasText(roomNumber)) {
            wrapper.like(Room::getRoomNumber, roomNumber);
        }
        if (StringUtils.hasText(purpose)) {
            wrapper.eq(Room::getPurpose, purpose);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Room::getStatus, status);
        }
        wrapper.orderByDesc(Room::getCreatedAt);

        Page<Room> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(r -> {
            Building building = buildingService.getById(r.getBuildingId());
            r.setBuildingName(building != null ? building.getName() : "");
            r.setDepartmentName(departmentService.getNameById(r.getDepartmentId()));
        });
        return page;
    }

    @Override
    public List<Room> listByBuilding(Long buildingId) {
        return lambdaQuery().eq(Room::getBuildingId, buildingId).orderByAsc(Room::getFloor).orderByAsc(Room::getRoomNumber).list();
    }

    @Override
    public void createRoom(RoomDTO dto) {
        // 业务校验：检查楼宇是否存在
        Building building = buildingService.getById(dto.getBuildingId());
        if (building == null) {
            throw new BusinessException("所属楼宇不存在");
        }

        // 业务校验：检查房间号在该楼宇内是否唯一
        long count = lambdaQuery()
                .eq(Room::getBuildingId, dto.getBuildingId())
                .eq(Room::getRoomNumber, dto.getRoomNumber())
                .count();
        if (count > 0) {
            throw new BusinessException("该楼宇内房间号【" + dto.getRoomNumber() + "】已存在");
        }

        // 业务校验：楼层不能超过楼宇总层数
        if (dto.getFloor() != null && building.getFloors() != null) {
            if (dto.getFloor() > building.getFloors()) {
                throw new BusinessException("楼层不能超过楼宇总层数【" + building.getFloors() + "】");
            }
        }

        Room room = new Room();
        BeanUtils.copyProperties(dto, room);
        if (!StringUtils.hasText(room.getStatus())) {
            room.setStatus("空闲");
        }
        save(room);
        log.info("新增房屋: {}", room.getRoomNumber());
    }

    @Override
    public void updateRoom(RoomDTO dto) {
        Room room = getById(dto.getId());
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 业务校验：检查房间号唯一性（排除自身）
        long count = lambdaQuery()
                .eq(Room::getBuildingId, dto.getBuildingId())
                .eq(Room::getRoomNumber, dto.getRoomNumber())
                .ne(Room::getId, dto.getId())
                .count();
        if (count > 0) {
            throw new BusinessException("该楼宇内房间号【" + dto.getRoomNumber() + "】已存在");
        }

        // 业务校验：楼层不能超过楼宇总层数
        Building building = buildingService.getById(dto.getBuildingId());
        if (building != null && dto.getFloor() != null && building.getFloors() != null) {
            if (dto.getFloor() > building.getFloors()) {
                throw new BusinessException("楼层不能超过楼宇总层数【" + building.getFloors() + "】");
            }
        }

        BeanUtils.copyProperties(dto, room);
        updateById(room);
        log.info("修改房屋: {}", room.getRoomNumber());
    }

    @Override
    public void updateStatus(Long id, String status) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }
        room.setStatus(status);
        updateById(room);
        log.info("变更房屋状态: {} -> {}", room.getRoomNumber(), status);
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = getById(id);
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 业务校验：检查房屋状态，不允许删除非空闲状态的房屋
        if (!"空闲".equals(room.getStatus())) {
            throw new BusinessException("房屋当前状态为【" + room.getStatus() + "】，不可删除");
        }

        removeById(id);
        log.info("删除房屋: {}", room.getRoomNumber());
    }
}
