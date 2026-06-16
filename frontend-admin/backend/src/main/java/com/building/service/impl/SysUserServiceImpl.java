package com.building.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.building.common.BusinessException;
import com.building.dto.LoginDTO;
import com.building.dto.UserDTO;
import com.building.entity.SysUser;
import com.building.mapper.SysUserMapper;
import com.building.security.JwtUtil;
import com.building.security.UserContext;
import com.building.service.SysDepartmentService;
import com.building.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysDepartmentService departmentService;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        SysUser user = lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername())
                .one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        // 密码校验：使用 MD5 加密比对
        String inputMd5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(inputMd5)) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getDepartmentId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("role", user.getRole());
        result.put("departmentId", user.getDepartmentId());
        result.put("departmentName", departmentService.getNameById(user.getDepartmentId()));

        log.info("用户登录成功: {}", user.getUsername());
        return result;
    }

    @Override
    public SysUser getCurrentUser() {
        UserContext ctx = UserContext.get();
        if (ctx == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = getById(ctx.getUserId());
        if (user != null) {
            user.setDepartmentName(departmentService.getNameById(user.getDepartmentId()));
        }
        return user;
    }

    @Override
    public Page<SysUser> pageList(int current, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreatedAt);
        Page<SysUser> page = page(new Page<>(current, size), wrapper);
        page.getRecords().forEach(u -> u.setDepartmentName(departmentService.getNameById(u.getDepartmentId())));
        return page;
    }

    @Override
    public void createUser(UserDTO dto) {
        // 业务校验：检查用户名唯一
        long count = lambdaQuery().eq(SysUser::getUsername, dto.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 业务校验：检查单位是否存在
        String deptName = departmentService.getNameById(dto.getDepartmentId());
        if (deptName == null) {
            throw new BusinessException("所选单位不存在");
        }

        // 业务校验：检查邮箱唯一性（如果填写了邮箱）
        if (StringUtils.hasText(dto.getEmail())) {
            long emailCount = lambdaQuery().eq(SysUser::getEmail, dto.getEmail()).count();
            if (emailCount > 0) {
                throw new BusinessException("该邮箱已被其他用户使用");
            }
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        // 默认密码 123456
        String password = StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456";
        user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        save(user);
        log.info("创建用户: {}", user.getUsername());
    }

    @Override
    public void updateUser(UserDTO dto) {
        SysUser user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 业务校验：检查单位是否存在
        String deptName = departmentService.getNameById(dto.getDepartmentId());
        if (deptName == null) {
            throw new BusinessException("所选单位不存在");
        }

        // 业务校验：检查邮箱唯一性（排除自身）
        if (StringUtils.hasText(dto.getEmail())) {
            long emailCount = lambdaQuery()
                    .eq(SysUser::getEmail, dto.getEmail())
                    .ne(SysUser::getId, dto.getId())
                    .count();
            if (emailCount > 0) {
                throw new BusinessException("该邮箱已被其他用户使用");
            }
        }

        // 业务校验：不允许将超级管理员的角色改为非管理员
        if ("admin".equals(user.getUsername()) && !"ADMIN".equals(dto.getRole())) {
            throw new BusinessException("不能修改超级管理员的角色");
        }

        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setDepartmentId(dto.getDepartmentId());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(dto.getStatus());
        updateById(user);
        log.info("更新用户: {}", user.getUsername());
    }

    @Override
    public void deleteUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除超级管理员");
        }
        removeById(id);
        log.info("删除用户: {}", user.getUsername());
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        updateById(user);
        log.info("重置用户密码: {}", user.getUsername());
    }
}
