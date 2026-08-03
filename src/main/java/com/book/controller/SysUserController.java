package com.book.controller;

import com.book.entity.SysUser;
import com.book.service.SysUserService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    // 分页查询
    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> pageData = sysUserService.page(page, new LambdaQueryWrapper<>());
        return Result.success(pageData);
    }

    // 根据id查询
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }

    // 新增
    @PostMapping("/add")
    public Result<Void> add(@RequestBody SysUser sysUser) {
        sysUserService.save(sysUser);
        return Result.success();
    }

    // 修改
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysUser sysUser) {
        sysUserService.updateById(sysUser);
        return Result.success();
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }
}