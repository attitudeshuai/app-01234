package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租赁记录实体类
 * <p>
 * 对应数据库表 rental，存储房屋对外租赁的信息，
 * 包括租赁方、租赁期限、租金、支付周期等属性。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("rental")
public class Rental {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 租赁房屋ID */
    private Long roomId;

    /** 租赁方名称（个人或企业） */
    private String tenantName;

    /** 租赁方联系方式 */
    private String tenantContact;

    /** 租赁方所属公司 */
    private String tenantCompany;

    /** 租赁起始日期 */
    private LocalDate startDate;

    /** 租赁结束日期 */
    private LocalDate endDate;

    /** 月租金（元） */
    private BigDecimal rentAmount;

    /** 支付周期：月付、季付、半年付、年付 */
    private String paymentCycle;

    /** 状态：有效、到期、终止 */
    private String status;

    /** 备注信息 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /* ========== 以下为非数据库字段，用于关联查询展示 ========== */

    /** 房间号（非数据库字段） */
    @TableField(exist = false)
    private String roomNumber;

    /** 所属楼宇名称（非数据库字段） */
    @TableField(exist = false)
    private String buildingName;
}
