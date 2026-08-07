package com.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.entity.BookInfo;
import com.book.entity.TradeRecord;
import com.book.entity.SysUser;
import com.book.exception.BusinessException;
import com.book.mapper.TradeRecordMapper;
import com.book.service.BookInfoService;
import com.book.service.SysUserService;
import com.book.service.TradeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TradeRecordServiceImpl extends ServiceImpl<TradeRecordMapper, TradeRecord> implements TradeRecordService {

    /** 订单状态：0-待确认 1-已成交 2-已取消 3-已拒绝 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_DONE = 1;
    private static final int STATUS_CANCELLED = 2;
    private static final int STATUS_REJECTED = 3;

    /** 图书状态：0-在售 1-已售 2-下架/停售 */
    private static final int BOOK_ON_SALE = 0;

    @Autowired
    private BookInfoService bookInfoService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyOrder(TradeRecord record) {
        BookInfo book = bookInfoService.getById(record.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getBookStatus() == null || book.getBookStatus() != BOOK_ON_SALE) {
            throw new BusinessException("该图书不在售，无法购买");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BusinessException("该图书缺货");
        }
        // 不能购买自己发布的图书
        if (book.getSellerId() != null && book.getSellerId().equals(record.getBuyerId())) {
            throw new BusinessException("不能购买自己发布的图书");
        }
        // 同一本书不可重复下单
        Long pending = count(new LambdaQueryWrapper<TradeRecord>()
                .eq(TradeRecord::getBookId, record.getBookId())
                .eq(TradeRecord::getStatus, STATUS_PENDING));
        if (pending > 0) {
            throw new BusinessException("该书已有待确认订单");
        }
        record.setId(null);
        record.setBookName(book.getBookName());
        record.setOrderPrice(book.getPrice());
        record.setSellerId(book.getSellerId());
        record.setSellerName(book.getSellerName());
        SysUser buyer = sysUserService.getById(record.getBuyerId());
        record.setBuyerName(buyer != null ? buyer.getRealName() : null);
        record.setStatus(STATUS_PENDING);
        record.setApplyTime(LocalDateTime.now());
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id, String remark) {
        TradeRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("订单不存在");
        }
        if (record.getStatus() != STATUS_PENDING) {
            throw new BusinessException("该订单不在待确认状态");
        }
        BookInfo book = bookInfoService.getById(record.getBookId());
        if (book == null || book.getBookStatus() == null || book.getBookStatus() != BOOK_ON_SALE) {
            throw new BusinessException("图书已不在售，无法成交");
        }
        record.setStatus(STATUS_DONE);
        record.setDoneTime(LocalDateTime.now());
        if (remark != null && !remark.isEmpty()) {
            record.setRemark(remark);
        }
        updateById(record);
        // 成交后扣减库存；库存扣完不下架，仅在书库显示缺货（stock=0）
        int stock = book.getStock() == null ? 0 : book.getStock();
        book.setStock(Math.max(0, stock - 1));
        bookInfoService.updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        TradeRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("订单不存在");
        }
        if (record.getStatus() != STATUS_PENDING) {
            throw new BusinessException("该订单不在待确认状态");
        }
        record.setStatus(STATUS_REJECTED);
        record.setDoneTime(LocalDateTime.now());
        if (remark != null && !remark.isEmpty()) {
            record.setRemark(remark);
        }
        updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        TradeRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("订单不存在");
        }
        if (record.getStatus() != STATUS_PENDING) {
            throw new BusinessException("该订单不在待确认状态");
        }
        record.setStatus(STATUS_CANCELLED);
        record.setDoneTime(LocalDateTime.now());
        updateById(record);
    }
}