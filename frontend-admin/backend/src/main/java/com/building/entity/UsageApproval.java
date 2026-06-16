package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("usage_approval")
public class UsageApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private Long departmentId;

    private String applicant;

    private String usageType;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private String reason;

    private String approver;

    private LocalDateTime approveTime;

    private String approveRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String roomNumber;

    @TableField(exist = false)
    private String buildingName;

    @TableField(exist = false)
    private String departmentName;
}
