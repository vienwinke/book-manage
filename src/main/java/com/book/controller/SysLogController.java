package com.book.controller;

import com.book.entity.SysLog;
import com.book.service.SysLogService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/log")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    @GetMapping("/page")
    public Result<Page<SysLog>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<SysLog> page = new Page<>(pageNum, pageSize);
        Page<SysLog> pageData = sysLogService.page(page, new LambdaQueryWrapper<>());
        return Result.success(pageData);
    }

    @GetMapping("/{id}")
    public Result<SysLog> getById(@PathVariable Long id) {
        SysLog log = sysLogService.getById(id);
        return Result.success(log);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysLogService.removeById(id);
        return Result.success();
    }
}