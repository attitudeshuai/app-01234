package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 房屋数据传输对象
 */
@Data
public class RoomDTO {

    private Long id;

    @NotNull(message = "所属楼宇不能为空")
    private Long buildingId;

    @NotNull(message = "所属单位不能为空")
    private Long departmentId;

    @NotBlank(message = "房间号不能为空")
    @Size(max = 50, message = "房间号不能超过50个字符")
    private String roomNumber;

    @Min(value = -10, message = "楼层不能低于-10层")
    @Max(value = 200, message = "楼层不能超过200层")
    private Integer floor;

    @DecimalMin(value = "0", message = "面积不能为负数")
    @Digits(integer = 10, fraction = 2, message = "面积格式不正确")
    private BigDecimal area;

    @Size(max = 50, message = "用途不能超过50个字符")
    private String purpose;

    @Size(max = 20, message = "状态不能超过20个字符")
    private String status;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
