package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资产数据传输对象
 */
@Data
public class AssetDTO {

    private Long id;

    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotBlank(message = "资产名称不能为空")
    @Size(max = 100, message = "资产名称不能超过100个字符")
    private String assetName;

    @Size(max = 50, message = "资产类型不能超过50个字符")
    private String assetType;

    @Min(value = 1, message = "数量至少为1")
    @Max(value = 999999, message = "数量不能超过999999")
    private Integer quantity;

    @Size(max = 20, message = "状态不能超过20个字符")
    private String status;

    private LocalDate purchaseDate;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
