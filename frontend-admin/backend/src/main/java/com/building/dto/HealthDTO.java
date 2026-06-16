package com.building.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 健康检查数据传输对象
 */
@Data
public class HealthDTO {

    private Long id;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotNull(message = "检查日期不能为空")
    private LocalDate checkDate;

    @NotBlank(message = "检查类型不能为空")
    @Size(max = 50, message = "检查类型不能超过50个字符")
    private String checkType;

    @NotBlank(message = "检查结果不能为空")
    @Size(max = 50, message = "检查结果不能超过50个字符")
    private String result;

    @Size(max = 500, message = "问题描述不能超过500个字符")
    private String description;

    @Size(max = 50, message = "处理人不能超过50个字符")
    private String handler;

    private LocalDate nextCheckDate;
}
