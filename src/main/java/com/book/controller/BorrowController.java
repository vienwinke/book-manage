package com.book.controller;

import com.book.entity.BorrowRecord;
import com.book.service.BorrowRecordService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    @GetMapping("/page")
    public Result<Page<BorrowRecord>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<BorrowRecord> page = new Page<>(pageNum, pageSize);
        Page<BorrowRecord> pageData = borrowRecordService.page(page, new LambdaQueryWrapper<>());
        return Result.success(pageData);
    }

    @GetMapping("/{id}")
    public Result<BorrowRecord> getById(@PathVariable Long id) {
        BorrowRecord record = borrowRecordService.getById(id);
        return Result.success(record);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody BorrowRecord borrowRecord) {
        borrowRecordService.save(borrowRecord);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody BorrowRecord borrowRecord) {
        borrowRecordService.updateById(borrowRecord);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        borrowRecordService.removeById(id);
        return Result.success();
    }
}