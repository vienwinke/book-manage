package com.book.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.book.entity.SysUser;
import com.book.annotation.OpLog;
import com.book.service.SysUserService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Result<Void> update(@Valid @RequestBody SysUser sysUser) {
        sysUserService.updateById(sysUser);
        return Result.success();
    }

    // 删除
    @OpLog(description = "删除用户", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }
}