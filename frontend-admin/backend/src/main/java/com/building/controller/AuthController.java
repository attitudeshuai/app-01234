package com.building.controller;

import com.building.common.Result;
import com.building.dto.LoginDTO;
import com.building.entity.SysUser;
import com.building.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        Map<String, Object> data = userService.login(dto);
        return Result.success("登录成功", data);
    }

    @GetMapping("/info")
    public Result<SysUser> info() {
        return Result.success(userService.getCurrentUser());
    }
}
