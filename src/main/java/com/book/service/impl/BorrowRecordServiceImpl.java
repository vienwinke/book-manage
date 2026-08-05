package com.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.entity.BookInfo;
import com.book.entity.BorrowRecord;
import com.book.exception.BusinessException;
import com.book.mapper.BorrowRecordMapper;
import com.book.service.BookInfoService;
import com.book.service.BorrowRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

    /** 状态：0-待审核 1-借出 2-已归还 3-已拒绝 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_BORROWED = 1;
    private static final int STATUS_RETURNED = 2;
    private static final int STATUS_REJECTED = 3;

    @Autowired
    private BookInfoService bookInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyBorrow(BorrowRecord record) {
        BookInfo book = bookInfoService.getById(record.getBookId());
        if (book == null) {
            throw new BusinessException("图书不存在");
        }
        if (book.getBookStatus() != null && book.getBookStatus() == 1) {
            throw new BusinessException("该图书已下架，不可借阅");
        }
        if (book.getAvailableNum() == null || book.getAvailableNum() <= 0) {
            throw new BusinessException("该图书当前无可借库存");
        }
        // 同一用户不允许同时有多个未完结的借阅
        List<Integer> ongoingStatus = Arrays.asList(STATUS_PENDING, STATUS_BORROWED);
        Long ongoing = count(new LambdaQueryWrapper<BorrowRecord>()
                .eq(BorrowRecord::getUserId, record.getUserId())
                .in(BorrowRecord::getStatus, ongoingStatus));
        if (ongoing > 0) {
            throw new BusinessException("您有待审核或未归还的借阅记录，暂不能申请");
        }
        record.setId(null);
        record.setStatus(STATUS_PENDING);
        record.setApplyTime(LocalDateTime.now());
        save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, String adminRemark) {
        BorrowRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (record.getStatus() != STATUS_PENDING) {
            throw new BusinessException("该记录不在待审核状态");
        }
        BookInfo book = bookInfoService.getById(record.getBookId());
        if (book == null || book.getAvailableNum() == null || book.getAvailableNum() <= 0) {
            throw new BusinessException("图书库存不足，无法借出");
        }
        record.setStatus(STATUS_BORROWED);
        record.setAdminRemark(adminRemark);
        updateById(record);
        // 库存减一
        book.setAvailableNum(book.getAvailableNum() - 1);
        bookInfoService.updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String adminRemark) {
        BorrowRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (record.getStatus() != STATUS_PENDING) {
            throw new BusinessException("该记录不在待审核状态");
        }
        record.setStatus(STATUS_REJECTED);
        record.setAdminRemark(adminRemark);
        updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnBook(Long id) {
        BorrowRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("借阅记录不存在");
        }
        if (record.getStatus() != STATUS_BORROWED) {
            throw new BusinessException("该记录未处于借出状态");
        }
        record.setStatus(STATUS_RETURNED);
        record.setActualReturnTime(LocalDateTime.now());
        updateById(record);
        // 库存加一
        BookInfo book = bookInfoService.getById(record.getBookId());
        if (book != null) {
            book.setAvailableNum(book.getAvailableNum() + 1);
            bookInfoService.updateById(book);
        }
    }
}
