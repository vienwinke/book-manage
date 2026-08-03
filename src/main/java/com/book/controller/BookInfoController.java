package com.book.controller;

import com.book.entity.BookInfo;
import com.book.service.BookInfoService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    // 分页查询
    @GetMapping("/page")
    public Result<Page<BookInfo>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        Page<BookInfo> pageData = bookInfoService.page(page, new LambdaQueryWrapper<>());
        return Result.success(pageData);
    }

    // 根据id查询
    @GetMapping("/{id}")
    public Result<BookInfo> getById(@PathVariable Long id) {
        BookInfo bookInfo = bookInfoService.getById(id);
        return Result.success(bookInfo);
    }

    // 新增图书
    @PostMapping("/add")
    public Result<Void> add(@RequestBody BookInfo bookInfo) {
        bookInfoService.save(bookInfo);
        return Result.success();
    }

    // 修改图书
    @PutMapping("/update")
    public Result<Void> update(@RequestBody BookInfo bookInfo) {
        bookInfoService.updateById(bookInfo);
        return Result.success();
    }

    // 删除图书
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookInfoService.removeById(id);
        return Result.success();
    }
}