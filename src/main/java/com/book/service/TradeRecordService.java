package com.book.service;

import com.book.entity.TradeRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TradeRecordService extends IService<TradeRecord> {

    /** 买家下单购买（校验在售、非卖家本人、无待确认订单） */
    void applyOrder(TradeRecord record);

    /** 管理员/卖家确认成交（图书标记已售） */
    void confirm(Long id, String remark);

    /** 管理员/卖家拒绝订单 */
    void reject(Long id, String remark);

    /** 买家取消订单 */
    void cancel(Long id);
}