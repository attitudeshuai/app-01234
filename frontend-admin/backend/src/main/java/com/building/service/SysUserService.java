package com.building.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.building.dto.LoginDTO;
import com.building.dto.UserDTO;
import com.building.entity.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    Map<String, Object> login(LoginDTO dto);

    SysUser getCurrentUser();

    Page<SysUser> pageList(int current, int size, String keyword);

    void createUser(UserDTO dto);

    void updateUser(UserDTO dto);

    void deleteUser(Long id);

    void resetPassword(Long id);
}
