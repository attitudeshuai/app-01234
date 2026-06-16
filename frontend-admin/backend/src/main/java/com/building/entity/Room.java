package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房屋实体类
 * <p>
 * 对应数据库表 room，存储房屋的基本信息，
 * 包括房间号、楼层、面积、用途、状态等属性。
 * 每个房屋隶属于一个楼宇和一个管理单位。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("room")
public class Room {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属楼宇ID */
    private Long buildingId;

    /** 所属单位ID */
    private Long departmentId;

    /** 房间号，如 101、A-201 */
    private String roomNumber;

    /** 所在楼层，支持负数表示地下层 */
    private Integer floor;

    /** 房屋面积（平方米） */
    private BigDecimal area;

    /** 用途：办公、宿舍、公寓、教学楼、病房、其他 */
    private String purpose;

    /** 状态：空闲、使用中、出租、封存 */
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

    /** 所属楼宇名称（非数据库字段） */
    @TableField(exist = false)
    private String buildingName;

    /** 所属单位名称（非数据库字段） */
    @TableField(exist = false)
    private String departmentName;
}
