package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.dto.HealthDTO;
import com.building.entity.Building;
import com.building.entity.Room;
import com.building.entity.RoomHealth;
import com.building.mapper.RoomHealthMapper;
import com.building.service.BuildingService;
import com.building.service.RoomHealthService;
import com.building.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class RoomHealthServiceImpl extends ServiceImpl<RoomHealthMapper, RoomHealth> implements RoomHealthService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService;

    @Override
    public Page<RoomHealth> pageList(int current, int size, Long roomId, String checkType, String result) {
        LambdaQueryWrapper<RoomHealth> wrapper = new LambdaQueryWrapper<>();
        if (roomId != null) {
            wrapper.eq(RoomHealth::getRoomId, roomId);
        }
        if (StringUtils.hasText(checkType)) {
            wrapper.eq(RoomHealth::getCheckType, checkType);
        }
        if (StringUtils.hasText(result)) {
            wrapper.eq(RoomHealth::getResult, result);
        }
        wrapper.orderByDesc(RoomHealth::getCheckDate);

        Page<RoomHealth> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(h -> {
            Room room = roomService.getById(h.getRoomId());
            if (room != null) {
                h.setRoomNumber(room.getRoomNumber());
                Building building = buildingService.getById(room.getBuildingId());
                h.setBuildingName(building != null ? building.getName() : "");
            }
        });
        return page;
    }

    @Override
    public void createHealth(HealthDTO dto) {
        // 业务校验：检查房屋是否存在
        Room room = roomService.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 业务校验：下次检查日期必须晚于检查日期
        if (dto.getNextCheckDate() != null && dto.getCheckDate() != null) {
            if (!dto.getNextCheckDate().isAfter(dto.getCheckDate())) {
                throw new BusinessException("下次检查日期必须晚于检查日期");
            }
        }

        RoomHealth health = new RoomHealth();
        BeanUtils.copyProperties(dto, health);
        save(health);
        log.info("新增健康检查记录: 房间ID={}, 类型={}", health.getRoomId(), health.getCheckType());
    }

    @Override
    public void updateHealth(HealthDTO dto) {
        RoomHealth health = getById(dto.getId());
        if (health == null) {
            throw new BusinessException("健康记录不存在");
        }

        // 业务校验：下次检查日期必须晚于检查日期
        if (dto.getNextCheckDate() != null && dto.getCheckDate() != null) {
            if (!dto.getNextCheckDate().isAfter(dto.getCheckDate())) {
                throw new BusinessException("下次检查日期必须晚于检查日期");
            }
        }

        BeanUtils.copyProperties(dto, health);
        updateById(health);
        log.info("修改健康检查记录: ID={}", health.getId());
    }
}
