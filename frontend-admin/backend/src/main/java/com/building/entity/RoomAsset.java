package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("room_asset")
public class RoomAsset {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private String assetName;

    private String assetType;

    private Integer quantity;

    private String status;

    private LocalDate purchaseDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String roomNumber;

    @TableField(exist = false)
    private String buildingName;
}
