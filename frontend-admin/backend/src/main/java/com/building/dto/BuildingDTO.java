package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 楼宇数据传输对象
 */
@Data
public class BuildingDTO {

    private Long id;

    @NotBlank(message = "楼宇名称不能为空")
    @Size(max = 100, message = "楼宇名称不能超过100个字符")
    private String name;

    @NotBlank(message = "楼宇编号不能为空")
    @Size(max = 50, message = "楼宇编号不能超过50个字符")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "楼宇编号只能包含字母、数字和横杠")
    private String code;

    @NotNull(message = "所属单位不能为空")
    private Long departmentId;

    @Size(max = 255, message = "地址不能超过255个字符")
    private String address;

    @Min(value = 1, message = "楼层数至少为1")
    @Max(value = 200, message = "楼层数不能超过200")
    private Integer floors;

    @DecimalMin(value = "0", message = "总面积不能为负数")
    @Digits(integer = 10, fraction = 2, message = "总面积格式不正确")
    private BigDecimal totalArea;

    @Size(max = 50, message = "结构类型不能超过50个字符")
    private String structureType;

    @Min(value = 1800, message = "建造年份不能早于1800年")
    @Max(value = 2100, message = "建造年份不能晚于2100年")
    private Integer buildYear;

    private Integer status;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
