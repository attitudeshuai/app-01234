package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("room_health")
public class RoomHealth {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roomId;

    private LocalDate checkDate;

    private String checkType;

    private String result;

    private String description;

    private String handler;

    private LocalDate nextCheckDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String roomNumber;

    @TableField(exist = false)
    private String buildingName;
}
