package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 使用审批数据传输对象
 */
@Data
public class ApprovalDTO {

    private Long id;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotNull(message = "申请单位不能为空")
    private Long departmentId;

    @NotBlank(message = "申请人不能为空")
    @Size(max = 50, message = "申请人姓名不能超过50个字符")
    private String applicant;

    @NotBlank(message = "用途类型不能为空")
    @Size(max = 50, message = "用途类型不能超过50个字符")
    private String usageType;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 500, message = "申请原因不能超过500个字符")
    private String reason;

    /** 审批意见（审批时使用） */
    @Size(max = 500, message = "审批意见不能超过500个字符")
    private String approveRemark;
}
