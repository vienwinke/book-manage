package com.book.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.book.entity.SysUser;
import com.book.annotation.OpLog;
import com.book.exception.BusinessException;
import com.book.service.SysUserService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    // 分页查询
    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        pageNum = Math.max(pageNum == null ? 1 : pageNum, 1);
        pageSize = Math.max(1, Math.min(pageSize, 50)); // 限制单页大小
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> pageData = sysUserService.page(page, new LambdaQueryWrapper<>());
        List<SysUser> records = pageData.getRecords();
        records.forEach(u -> u.setPassword(null));
        pageData.setRecords(records);
        return Result.success(pageData);
    }

    // 根据id查询
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    // 新增
    @OpLog(description = "新增用户", type = 1)
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody SysUser sysUser) {
        sysUser.setPassword(BCrypt.hashpw(sysUser.getPassword(), BCrypt.gensalt()));
        if (sysUser.getUserType() == null) {
            sysUser.setUserType(0);
        }
        if (sysUser.getStatus() == null) {
            sysUser.setStatus(1);
        }
        sysUserService.save(sysUser);
        return Result.success();
    }

    // 修改
    @OpLog(description = "修改用户", type = 2)
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysUser sysUser) {
        if (sysUser.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser old = sysUserService.getById(sysUser.getId());
        if (old == null) {
            throw new BusinessException("用户不存在");
        }
        // 用户名: 传了才更新并校验格式，不传则不修改
        if (StringUtils.hasText(sysUser.getUsername())) {
            if (!sysUser.getUsername().matches("^[A-Za-z0-9]+$")) {
                throw new BusinessException("账号只能包含英文字母和数字，不能使用汉字");
            }
        } else {
            sysUser.setUsername(null);
        }
        // 密码: 传了则 BCrypt 加密后更新；不传则不修改，避免明文密码入库
        if (StringUtils.hasText(sysUser.getPassword())) {
            sysUser.setPassword(BCrypt.hashpw(sysUser.getPassword(), BCrypt.gensalt()));
        } else {
            sysUser.setPassword(null); // MyBatis-Plus 忽略 null 字段
        }
        sysUserService.updateById(sysUser);
        // 若用户被禁用（status=0），立即注销其所有会话
        if (sysUser.getStatus() != null && sysUser.getStatus() == 0
                && (old.getStatus() == null || old.getStatus() != 0)) {
            StpUtil.logout(sysUser.getId());
        }
        return Result.success();
    }

    // 删除（删除后立即注销该用户的所有会话，防止 token 继续使用）
    @OpLog(description = "删除用户", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        StpUtil.logout(id);
        return Result.success();
    }
}