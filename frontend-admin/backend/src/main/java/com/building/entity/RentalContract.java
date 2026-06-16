package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("rental_contract")
public class RentalContract {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long rentalId;

    private String contractNumber;

    private LocalDate signDate;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal totalAmount;

    private String status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String tenantName;

    @TableField(exist = false)
    private String roomNumber;
}
