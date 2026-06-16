package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.ApprovalDTO;
import com.building.entity.UsageApproval;
import com.building.service.UsageApprovalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/approvals")
public class UsageApprovalController {

    @Autowired
    private UsageApprovalService approvalService;

    @GetMapping
    public Result<PageResult<UsageApproval>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String usageType) {
        return Result.success(PageResult.of(approvalService.pageList(current, size, departmentId, status, usageType)));
    }

    @PostMapping
    @OperLog(module = "使用审批", action = "提交申请")
    public Result<Void> create(@Valid @RequestBody ApprovalDTO dto) {
        approvalService.createApproval(dto);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    @OperLog(module = "使用审批", action = "审批通过")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("approveRemark") : null;
        approvalService.approve(id, remark);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    @OperLog(module = "使用审批", action = "审批驳回")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("approveRemark") : null;
        approvalService.reject(id, remark);
        return Result.success();
    }
}
