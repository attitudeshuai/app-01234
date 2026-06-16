package com.building.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须以字母开头，只能包含字母、数字和下划线")
    private String username;

    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(ADMIN|MANAGER)$", message = "角色必须为ADMIN或MANAGER")
    private String role;

    @NotNull(message = "所属单位不能为空")
    private Long departmentId;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String phone;

    @Email(message = "请输入正确的邮箱地址")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    private Integer status;
}
