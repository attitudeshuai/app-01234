package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.common.Constants;
import com.building.dto.BuildingDTO;
import com.building.entity.Building;
import com.building.mapper.BuildingMapper;
import com.building.mapper.RoomMapper;
import com.building.security.UserContext;
import com.building.service.BuildingService;
import com.building.service.SysDepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 楼宇管理服务实现类
 * <p>
 * 实现楼宇的增删改查业务逻辑，包括：
 * <ul>
 *   <li>数据权限过滤：非管理员只能操作本单位数据</li>
 *   <li>楼宇编号唯一性校验</li>
 *   <li>删除前检查是否存在关联房屋</li>
 *   <li>自动填充单位名称和房屋数量</li>
 * </ul>
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Slf4j
@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {

    @Autowired
    private SysDepartmentService departmentService;

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public Page<Building> pageList(int current, int size, String name, Long departmentId, Integer status) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();

        // 数据权限过滤
        UserContext ctx = UserContext.get();
        if (ctx != null && !ctx.isAdmin()) {
            wrapper.eq(Building::getDepartmentId, ctx.getDepartmentId());
        }

        if (StringUtils.hasText(name)) {
            wrapper.like(Building::getName, name);
        }
        if (departmentId != null) {
            wrapper.eq(Building::getDepartmentId, departmentId);
        }
        if (status != null) {
            wrapper.eq(Building::getStatus, status);
        }
        wrapper.orderByDesc(Building::getCreatedAt);

        Page<Building> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(b -> {
            b.setDepartmentName(departmentService.getNameById(b.getDepartmentId()));
            b.setRoomCount(roomMapper.countByBuildingId(b.getId()));
        });
        return page;
    }

    @Override
    public List<Building> listAll() {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        UserContext ctx = UserContext.get();
        if (ctx != null && !ctx.isAdmin()) {
            wrapper.eq(Building::getDepartmentId, ctx.getDepartmentId());
        }
        wrapper.eq(Building::getStatus, Constants.BUILDING_STATUS_NORMAL);
        wrapper.orderByAsc(Building::getCode);
        return list(wrapper);
    }

    @Override
    public void createBuilding(BuildingDTO dto) {
        long count = lambdaQuery().eq(Building::getCode, dto.getCode()).count();
        if (count > 0) {
            throw new BusinessException("楼宇编号已存在");
        }
        Building building = new Building();
        BeanUtils.copyProperties(dto, building);
        building.setStatus(Constants.BUILDING_STATUS_NORMAL);
        save(building);
        log.info("新增楼宇: {} ({})", building.getName(), building.getCode());
    }

    @Override
    public void updateBuilding(BuildingDTO dto) {
        Building building = getById(dto.getId());
        if (building == null) {
            throw new BusinessException("楼宇不存在");
        }
        BeanUtils.copyProperties(dto, building);
        updateById(building);
        log.info("修改楼宇: {} ({})", building.getName(), building.getCode());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Building building = getById(id);
        if (building == null) {
            throw new BusinessException("楼宇不存在");
        }
        building.setStatus(status);
        updateById(building);
        log.info("变更楼宇状态: {} -> {}", building.getName(), status == 1 ? "正常" : "封存");
    }

    @Override
    public void deleteBuilding(Long id) {
        Building building = getById(id);
        if (building == null) {
            throw new BusinessException("楼宇不存在");
        }
        int roomCount = roomMapper.countByBuildingId(id);
        if (roomCount > 0) {
            throw new BusinessException("该楼宇下存在房屋，无法删除");
        }
        removeById(id);
        log.info("删除楼宇: {}", building.getName());
    }
}
