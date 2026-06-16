package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 租赁数据传输对象
 */
@Data
public class RentalDTO {

    private Long id;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotBlank(message = "租赁方名称不能为空")
    @Size(max = 100, message = "租赁方名称不能超过100个字符")
    private String tenantName;

    @Size(max = 20, message = "联系方式不能超过20个字符")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String tenantContact;

    @Size(max = 200, message = "租赁公司不能超过200个字符")
    private String tenantCompany;

    @NotNull(message = "起始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotNull(message = "月租金不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "月租金必须大于0")
    @Digits(integer = 10, fraction = 2, message = "月租金格式不正确")
    private BigDecimal rentAmount;

    @Size(max = 20, message = "支付周期不能超过20个字符")
    private String paymentCycle;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
