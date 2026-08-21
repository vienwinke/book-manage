package com.book.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.book.entity.BookInfo;
import com.book.entity.SysUser;
import com.book.entity.TradeRecord;
import com.book.annotation.OpLog;
import com.book.exception.BusinessException;
import com.book.service.BookInfoService;
import com.book.service.SysUserService;
import com.book.service.TradeRecordService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/book")
public class BookInfoController {

    @Autowired
    private BookInfoService bookInfoService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private TradeRecordService tradeRecordService;

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
        pageNum = Math.max(pageNum == null ? 1 : pageNum, 1);
        pageSize = Math.max(1, Math.min(pageSize, 50)); // 限制单页大小，防止拉取全表
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
        // 售价必须大于 0
        if (bookInfo.getPrice() == null || bookInfo.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("售价必须大于 0");
        }
        bookInfo.setBookStatus(0); // 上架统一为在售，忽略请求体传入的状态
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
        // 图书所有权不允许通过 update 篡改，恢复为原值
        bookInfo.setSellerId(old.getSellerId());
        bookInfo.setSellerName(old.getSellerName());
        if (bookInfo.getStock() == null) {
            bookInfo.setStock(old.getStock() == null ? 1 : old.getStock());
        } else if (bookInfo.getStock() < 0) {
            bookInfo.setStock(0);
        }
        bookInfoService.updateById(bookInfo);
        return Result.success();
    }

    // 删除图书（卖家本人或管理员；存在待确认订单时禁止删除）
    @OpLog(description = "删除图书", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        BookInfo old = bookInfoService.getById(id);
        if (old == null) {
            throw new BusinessException("图书不存在");
        }
        checkOwnerOrAdmin(old);
        long pendingCount = tradeRecordService.count(new LambdaQueryWrapper<TradeRecord>()
                .eq(TradeRecord::getBookId, id)
                .eq(TradeRecord::getStatus, 0));
        if (pendingCount > 0) {
            throw new BusinessException("该书存在待确认订单，无法删除");
        }
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