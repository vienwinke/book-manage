package com.book.controller;

import com.book.entity.BorrowRecord;
import com.book.annotation.OpLog;
import com.book.service.BorrowRecordService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/borrow")
public class BorrowController {

    @Autowired
    private BorrowRecordService borrowRecordService;

    // 分页查询（支持按借阅人/状态筛选）
    @GetMapping("/page")
    public Result<Page<BorrowRecord>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) Integer status) {
        Page<BorrowRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, BorrowRecord::getUserId, userId)
                .eq(status != null, BorrowRecord::getStatus, status)
                .orderByDesc(BorrowRecord::getApplyTime);
        Page<BorrowRecord> pageData = borrowRecordService.page(page, wrapper);
        return Result.success(pageData);
    }

    // 根据id查询
    @GetMapping("/{id}")
    public Result<BorrowRecord> getById(@PathVariable Long id) {
        BorrowRecord record = borrowRecordService.getById(id);
        return Result.success(record);
    }

    // 学生申请借阅
    @OpLog(description = "申请借阅", type = 1)
    @PostMapping("/apply")
    public Result<Void> apply(@Valid @RequestBody BorrowRecord borrowRecord) {
        borrowRecordService.applyBorrow(borrowRecord);
        return Result.success();
    }

    // 管理员审核通过（借出）
    @OpLog(description = "审核通过借阅", type = 2)
    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id,
                                @RequestBody(required = false) Map<String, String> body) {
        borrowRecordService.approve(id, body == null ? null : body.get("adminRemark"));
        return Result.success();
    }

    // 管理员拒绝
    @OpLog(description = "拒绝借阅", type = 2)
    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        borrowRecordService.reject(id, body == null ? null : body.get("adminRemark"));
        return Result.success();
    }

    // 归还图书
    @OpLog(description = "归还图书", type = 2)
    @PostMapping("/return/{id}")
    public Result<Void> returnBook(@PathVariable Long id) {
        borrowRecordService.returnBook(id);
        return Result.success();
    }

    // 删除记录（仅管理员）
    @OpLog(description = "删除借阅记录", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        borrowRecordService.removeById(id);
        return Result.success();
    }
}
