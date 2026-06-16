package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.ApprovalDTO;
import com.building.entity.UsageApproval;

public interface UsageApprovalService extends IService<UsageApproval> {

    Page<UsageApproval> pageList(int current, int size, Long departmentId, String status, String usageType);

    void createApproval(ApprovalDTO dto);

    void approve(Long id, String approveRemark);

    void reject(Long id, String approveRemark);
}
