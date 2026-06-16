package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.common.Constants;
import com.building.dto.ApprovalDTO;
import com.building.entity.Building;
import com.building.entity.Room;
import com.building.entity.UsageApproval;
import com.building.mapper.UsageApprovalMapper;
import com.building.security.UserContext;
import com.building.service.BuildingService;
import com.building.service.RoomService;
import com.building.service.SysDepartmentService;
import com.building.service.UsageApprovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UsageApprovalServiceImpl extends ServiceImpl<UsageApprovalMapper, UsageApproval> implements UsageApprovalService {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private SysDepartmentService departmentService;

    @Override
    public Page<UsageApproval> pageList(int current, int size, Long departmentId, String status, String usageType) {
        LambdaQueryWrapper<UsageApproval> wrapper = new LambdaQueryWrapper<>();

        UserContext ctx = UserContext.get();
        if (ctx != null && !ctx.isAdmin()) {
            wrapper.eq(UsageApproval::getDepartmentId, ctx.getDepartmentId());
        }

        if (departmentId != null) {
            wrapper.eq(UsageApproval::getDepartmentId, departmentId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(UsageApproval::getStatus, status);
        }
        if (StringUtils.hasText(usageType)) {
            wrapper.eq(UsageApproval::getUsageType, usageType);
        }
        wrapper.orderByDesc(UsageApproval::getCreatedAt);

        Page<UsageApproval> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(a -> {
            Room room = roomService.getById(a.getRoomId());
            if (room != null) {
                a.setRoomNumber(room.getRoomNumber());
                Building building = buildingService.getById(room.getBuildingId());
                a.setBuildingName(building != null ? building.getName() : "");
            }
            a.setDepartmentName(departmentService.getNameById(a.getDepartmentId()));
        });
        return page;
    }

    @Override
    public void createApproval(ApprovalDTO dto) {
        // 业务校验：检查房屋是否存在
        Room room = roomService.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房屋不存在");
        }

        // 业务校验：检查房屋状态是否为空闲
        if (!Constants.ROOM_STATUS_FREE.equals(room.getStatus())) {
            throw new BusinessException("该房屋当前状态为【" + room.getStatus() + "】，不可申请使用");
        }

        // 业务校验：日期范围
        if (dto.getEndDate() != null && dto.getStartDate() != null) {
            if (!dto.getEndDate().isAfter(dto.getStartDate())) {
                throw new BusinessException("结束日期必须晚于开始日期");
            }
        }

        // 业务校验：检查单位是否存在
        String deptName = departmentService.getNameById(dto.getDepartmentId());
        if (deptName == null) {
            throw new BusinessException("所选单位不存在");
        }

        UsageApproval approval = new UsageApproval();
        BeanUtils.copyProperties(dto, approval);
        approval.setStatus(Constants.APPROVAL_PENDING);
        save(approval);
        log.info("提交使用申请: 房间ID={}, 申请人={}", dto.getRoomId(), dto.getApplicant());
    }

    @Override
    public void approve(Long id, String approveRemark) {
        UsageApproval approval = getById(id);
        if (approval == null) {
            throw new BusinessException("审批记录不存在");
        }
        if (!Constants.APPROVAL_PENDING.equals(approval.getStatus())) {
            throw new BusinessException("该申请已处理，不可重复审批");
        }

        UserContext ctx = UserContext.get();
        approval.setStatus(Constants.APPROVAL_APPROVED);
        approval.setApprover(ctx != null ? ctx.getUsername() : "system");
        approval.setApproveTime(LocalDateTime.now());
        approval.setApproveRemark(approveRemark);
        updateById(approval);

        // 更新房屋状态为使用中
        Room room = roomService.getById(approval.getRoomId());
        if (room != null) {
            room.setStatus(Constants.ROOM_STATUS_IN_USE);
            roomService.updateById(room);
        }

        log.info("审批通过: ID={}", id);
    }

    @Override
    public void reject(Long id, String approveRemark) {
        UsageApproval approval = getById(id);
        if (approval == null) {
            throw new BusinessException("审批记录不存在");
        }
        if (!Constants.APPROVAL_PENDING.equals(approval.getStatus())) {
            throw new BusinessException("该申请已处理，不可重复审批");
        }

        UserContext ctx = UserContext.get();
        approval.setStatus(Constants.APPROVAL_REJECTED);
        approval.setApprover(ctx != null ? ctx.getUsername() : "system");
        approval.setApproveTime(LocalDateTime.now());
        approval.setApproveRemark(approveRemark);
        updateById(approval);

        log.info("审批驳回: ID={}", id);
    }
}
