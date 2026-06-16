package com.building.controller;

import com.building.aspect.OperLog;
import com.building.common.PageResult;
import com.building.common.Result;
import com.building.dto.UserDTO;
import com.building.entity.SysUser;
import com.building.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class SysUserController {

    @Autowired
    private SysUserService userService;

    @GetMapping
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(userService.pageList(current, size, keyword)));
    }

    @PostMapping
    @OperLog(module = "用户管理", action = "新增")
    public Result<Void> create(@Valid @RequestBody UserDTO dto) {
        userService.createUser(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    @OperLog(module = "用户管理", action = "修改")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        dto.setId(id);
        userService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @OperLog(module = "用户管理", action = "删除")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @OperLog(module = "用户管理", action = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
}
