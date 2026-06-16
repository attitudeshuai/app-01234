package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 楼宇实体类
 * <p>
 * 对应数据库表 building，存储楼宇的基本信息，
 * 包括名称、编号、地址、楼层数、面积等属性。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("building")
public class Building {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 楼宇名称 */
    private String name;

    /** 楼宇编号，如 BLD-001 */
    private String code;

    /** 所属单位ID */
    private Long departmentId;

    /** 详细地址 */
    private String address;

    /** 楼层数 */
    private Integer floors;

    /** 总面积（平方米） */
    private BigDecimal totalArea;

    /** 结构类型：钢筋混凝土、框架结构、钢结构、砖混结构、其他 */
    private String structureType;

    /** 建造年份 */
    private Integer buildYear;

    /** 状态：1-正常，0-封存 */
    private Integer status;

    /** 备注信息 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /* ========== 以下为非数据库字段，用于关联查询展示 ========== */

    /** 所属单位名称（非数据库字段） */
    @TableField(exist = false)
    private String departmentName;

    /** 房屋数量（非数据库字段） */
    @TableField(exist = false)
    private Integer roomCount;
}
