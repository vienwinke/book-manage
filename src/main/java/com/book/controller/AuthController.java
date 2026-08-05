package com.book.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.book.annotation.OpLog;
import com.book.entity.SysUser;
import com.book.service.SysUserService;
import com.book.util.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：登录 / 注册 / 退出 / 当前用户信息
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    // 登录
    @OpLog(description = "用户登录", type = 4)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody SysUser user) {
        SysUser loginUser = sysUserService.login(user.getUsername(), user.getPassword());
        StpUtil.login(loginUser.getId());
        loginUser.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("user", loginUser);
        return Result.success(data);
    }

    // 注册（学生自助注册）
    @OpLog(description = "用户注册", type = 1)
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody SysUser user) {
        sysUserService.register(user);
        return Result.success();
    }

    // 退出登录
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    // 当前登录用户信息
    @GetMapping("/info")
    public Result<SysUser> info() {
        Long id = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(id);
        user.setPassword(null);
        return Result.success(user);
    }
}
