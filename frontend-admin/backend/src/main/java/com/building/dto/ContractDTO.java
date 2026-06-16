package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合约数据传输对象
 */
@Data
public class ContractDTO {

    private Long id;

    @NotNull(message = "租赁ID不能为空")
    private Long rentalId;

    @NotBlank(message = "合同编号不能为空")
    @Size(max = 50, message = "合同编号不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "合同编号只能包含字母、数字和横杠")
    private String contractNumber;

    @NotNull(message = "签约日期不能为空")
    private LocalDate signDate;

    @NotNull(message = "合同起始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "合同终止日期不能为空")
    private LocalDate endDate;

    @NotNull(message = "合同总额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "合同总额必须大于0")
    @Digits(integer = 12, fraction = 2, message = "合同总额格式不正确")
    private BigDecimal totalAmount;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
