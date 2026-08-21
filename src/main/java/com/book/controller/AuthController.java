package com.book.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.book.annotation.OpLog;
import com.book.entity.SysUser;
import com.book.exception.BusinessException;
import com.book.service.LoginAttemptService;
import com.book.service.SysUserService;
import com.book.util.Result;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private LoginAttemptService loginAttemptService;

    // 登录（含失败锁定：同一 IP+用户名 连续失败 5 次锁定 15 分钟）
    @OpLog(description = "用户登录", type = 4)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody SysUser user,
                                             HttpServletRequest request) {
        String key = clientIp(request) + ":" + user.getUsername();
        if (loginAttemptService.isLocked(key)) {
            throw new BusinessException("登录失败次数过多，请 "
                    + loginAttemptService.getLockRemainingSeconds(key) + " 秒后再试");
        }
        try {
            SysUser loginUser = sysUserService.login(user.getUsername(), user.getPassword());
            StpUtil.login(loginUser.getId());
            loginUser.setPassword(null);
            Map<String, Object> data = new HashMap<>();
            data.put("token", StpUtil.getTokenValue());
            data.put("user", loginUser);
            loginAttemptService.clear(key);
            return Result.success(data);
        } catch (BusinessException e) {
            loginAttemptService.recordFailure(key);
            throw e;
        }
    }

    // 注册（学生自助注册；按 IP 限流，防止批量注册）
    @OpLog(description = "用户注册", type = 1)
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody SysUser user,
                                 HttpServletRequest request) {
        if (!loginAttemptService.allowRegister(clientIp(request))) {
            throw new BusinessException("注册过于频繁，请稍后再试");
        }
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
        if (user == null) {
            throw new BusinessException("用户不存在，请重新登录");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            int comma = ip.indexOf(',');
            String first = (comma > 0 ? ip.substring(0, comma) : ip).trim();
            if (!first.isEmpty() && !"unknown".equalsIgnoreCase(first)) {
                return first;
            }
        }
        return request.getRemoteAddr();
    }
}
