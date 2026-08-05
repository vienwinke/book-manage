package com.book.controller;

import com.book.entity.BookInfo;
import com.book.annotation.OpLog;
import com.book.service.BookInfoService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    // 分页查询（支持书名/作者/分类模糊搜索、状态精确筛选）
    @GetMapping("/page")
    public Result<Page<BookInfo>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String bookName,
                                       @RequestParam(required = false) String author,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) Integer bookStatus) {
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(bookName), BookInfo::getBookName, bookName)
                .like(StringUtils.hasText(author), BookInfo::getAuthor, author)
                .like(StringUtils.hasText(category), BookInfo::getCategory, category)
                .eq(bookStatus != null, BookInfo::getBookStatus, bookStatus)
                .orderByDesc(BookInfo::getCreateTime);
        Page<BookInfo> pageData = bookInfoService.page(page, wrapper);
        return Result.success(pageData);
    }

    // 根据id查询
    @GetMapping("/{id}")
    public Result<BookInfo> getById(@PathVariable Long id) {
        BookInfo bookInfo = bookInfoService.getById(id);
        return Result.success(bookInfo);
    }

    // 新增图书
    @OpLog(description = "新增图书", type = 1)
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody BookInfo bookInfo) {
        if (bookInfo.getAvailableNum() == null) {
            bookInfo.setAvailableNum(bookInfo.getTotalNum());
        }
        if (bookInfo.getBookStatus() == null) {
            bookInfo.setBookStatus(0);
        }
        bookInfoService.save(bookInfo);
        return Result.success();
    }

    // 修改图书
    @OpLog(description = "修改图书", type = 2)
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody BookInfo bookInfo) {
        bookInfoService.updateById(bookInfo);
        return Result.success();
    }

    // 删除图书
    @OpLog(description = "删除图书", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bookInfoService.removeById(id);
        return Result.success();
    }
}
