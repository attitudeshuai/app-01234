package com.building.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * <p>
 * 对应数据库表 sys_user，存储系统用户的账号信息，
 * 包括用户名、密码、角色、所属单位等属性。
 * 支持集团管理员(ADMIN)和单位管理员(MANAGER)两种角色。
 * </p>
 *
 * @author system
 * @since 1.0
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，唯一 */
    private String username;

    /** 登录密码，MD5加密存储，JSON序列化时忽略 */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色：ADMIN-集团管理员，MANAGER-单位管理员 */
    private String role;

    /** 所属单位ID */
    private Long departmentId;

    /** 手机号码 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 状态：1-启用，0-禁用 */
    private Integer status;

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
}
