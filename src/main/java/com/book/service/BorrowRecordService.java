package com.book.service;

import com.book.entity.BorrowRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BorrowRecordService extends IService<BorrowRecord> {

    /**
     * 学生申请借阅（校验图书与库存，状态置为待审核）
     */
    void applyBorrow(BorrowRecord record);

    /**
     * 管理员审核通过（借出，库存减一）
     */
    void approve(Long id, String adminRemark);

    /**
     * 管理员拒绝借阅申请
     */
    void reject(Long id, String adminRemark);

    /**
     * 归还图书（库存加一）
     */
    void returnBook(Long id);
}
