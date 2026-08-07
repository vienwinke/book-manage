package com.book.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.book.entity.BookInfo;
import com.book.entity.SysUser;
import com.book.annotation.OpLog;
import com.book.exception.BusinessException;
import com.book.service.BookInfoService;
import com.book.service.SysUserService;
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

    @Autowired
    private SysUserService sysUserService;

    // 分页查询（支持书名/作者/分类模糊搜索、ISBN精确、卖家、状态精确筛选）
    @GetMapping("/page")
    public Result<Page<BookInfo>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String bookName,
                                       @RequestParam(required = false) String author,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String isbn,
                                       @RequestParam(required = false) Long sellerId,
                                       @RequestParam(required = false) Integer bookStatus) {
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(bookName), BookInfo::getBookName, bookName)
                .like(StringUtils.hasText(author), BookInfo::getAuthor, author)
                .like(StringUtils.hasText(category), BookInfo::getCategory, category)
                .eq(StringUtils.hasText(isbn), BookInfo::getIsbn, isbn)
                .eq(sellerId != null, BookInfo::getSellerId, sellerId)
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

    // 新增图书（登录用户可上架，自动记录卖家为当前登录用户）
    @OpLog(description = "上架图书", type = 1)
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody BookInfo bookInfo) {
        if (bookInfo.getBookStatus() == null) {
            bookInfo.setBookStatus(0);
        }
        if (bookInfo.getStock() == null || bookInfo.getStock() < 0) {
            bookInfo.setStock(1);
        }
        Long sellerId = StpUtil.getLoginIdAsLong();
        SysUser seller = sysUserService.getById(sellerId);
        bookInfo.setSellerId(sellerId);
        bookInfo.setSellerName(seller != null ? seller.getRealName() : null);
        bookInfoService.save(bookInfo);
        return Result.success();
    }

    // 修改图书（卖家本人或管理员）
    @OpLog(description = "修改图书", type = 2)
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody BookInfo bookInfo) {
        BookInfo old = bookInfoService.getById(bookInfo.getId());
        if (old == null) {
            throw new BusinessException("图书不存在");
        }
        checkOwnerOrAdmin(old);
        if (bookInfo.getStock() == null) {
            bookInfo.setStock(old.getStock() == null ? 1 : old.getStock());
        } else if (bookInfo.getStock() < 0) {
            bookInfo.setStock(0);
        }
        bookInfoService.updateById(bookInfo);
        return Result.success();
    }

    // 删除图书（卖家本人或管理员）
    @OpLog(description = "删除图书", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        BookInfo old = bookInfoService.getById(id);
        if (old == null) {
            throw new BusinessException("图书不存在");
        }
        checkOwnerOrAdmin(old);
        bookInfoService.removeById(id);
        return Result.success();
    }

    // 校验：管理员或本书卖家本人
    private void checkOwnerOrAdmin(BookInfo book) {
        Long loginId = StpUtil.getLoginIdAsLong();
        if (StpUtil.hasRole("admin")) {
            return;
        }
        if (book.getSellerId() == null || !book.getSellerId().equals(loginId)) {
            throw new BusinessException("无权操作他人图书");
        }
    }
}