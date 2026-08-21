package com.book.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.book.annotation.OpLog;
import com.book.entity.TradeRecord;
import com.book.exception.BusinessException;
import com.book.service.TradeRecordService;
import com.book.util.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 订单接口：下单 / 确认成交 / 拒绝 / 取消 / 删除
 * 暂不涉及付款通道，确认成交即完成交易
 */
@RestController
@RequestMapping("/order")
public class TradeController {

    @Autowired
    private TradeRecordService tradeRecordService;

    // 分页查询（普通用户只能看到自己的订单，管理员可看全部）
    @GetMapping("/page")
    public Result<Page<TradeRecord>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(required = false) Long buyerId) {
        pageNum = Math.max(pageNum == null ? 1 : pageNum, 1);
        pageSize = Math.max(1, Math.min(pageSize, 50)); // 限制单页大小
        Page<TradeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TradeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, TradeRecord::getStatus, status)
                .eq(buyerId != null, TradeRecord::getBuyerId, buyerId)
                .orderByDesc(TradeRecord::getApplyTime);
        // 越权防护：非管理员只能查自己相关订单（自己买的或自己卖的）
        if (!StpUtil.hasRole("admin")) {
            Long loginId = StpUtil.getLoginIdAsLong();
            wrapper.and(w -> w.eq(TradeRecord::getBuyerId, loginId)
                    .or().eq(TradeRecord::getSellerId, loginId));
        }
        Page<TradeRecord> pageData = tradeRecordService.page(page, wrapper);
        return Result.success(pageData);
    }

    @GetMapping("/{id}")
    public Result<TradeRecord> getById(@PathVariable Long id) {
        TradeRecord record = tradeRecordService.getById(id);
        if (record == null) {
            return Result.success(null);
        }
        // 越权防护：非管理员只能查看自己相关订单（自己买的或自己卖的）
        if (!StpUtil.hasRole("admin")) {
            Long loginId = StpUtil.getLoginIdAsLong();
            boolean related = (record.getBuyerId() != null && record.getBuyerId().equals(loginId))
                    || (record.getSellerId() != null && record.getSellerId().equals(loginId));
            if (!related) {
                throw new BusinessException("无权查看该订单");
            }
        }
        return Result.success(record);
    }

    // 买家下单购买（buyerId 取当前登录用户，防止越权）
    @OpLog(description = "下单购买", type = 1)
    @PostMapping("/apply")
    public Result<Void> apply(@RequestBody Map<String, Long> body) {
        Long bookId = body.get("bookId");
        if (bookId == null) {
            throw new BusinessException("请选择要购买的图书");
        }
        TradeRecord record = new TradeRecord();
        record.setBookId(bookId);
        record.setBuyerId(StpUtil.getLoginIdAsLong());
        tradeRecordService.applyOrder(record);
        return Result.success();
    }

    // 卖家/管理员确认成交
    @OpLog(description = "确认成交", type = 2)
    @PostMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id,
                                @RequestBody(required = false) Map<String, String> body) {
        checkSellerOrAdmin(id);
        tradeRecordService.confirm(id, body == null ? null : body.get("remark"));
        return Result.success();
    }

    // 卖家/管理员拒绝订单
    @OpLog(description = "拒绝订单", type = 2)
    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, String> body) {
        checkSellerOrAdmin(id);
        tradeRecordService.reject(id, body == null ? null : body.get("remark"));
        return Result.success();
    }

    // 校验：管理员或订单卖家本人
    private void checkSellerOrAdmin(Long id) {
        TradeRecord record = tradeRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("订单不存在");
        }
        Long loginId = StpUtil.getLoginIdAsLong();
        if (StpUtil.hasRole("admin")) {
            return;
        }
        if (record.getSellerId() == null || !record.getSellerId().equals(loginId)) {
            throw new BusinessException("只有卖家本人或管理员可操作该订单");
        }
    }

    // 买家取消订单（需本人或管理员）
    @OpLog(description = "取消订单", type = 2)
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        TradeRecord record = tradeRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("订单不存在");
        }
        if (!StpUtil.hasRole("admin") && !record.getBuyerId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("无权取消该订单");
        }
        tradeRecordService.cancel(id);
        return Result.success();
    }

    // 管理员删除订单（已成交订单不可删除，保留交易凭证）
    @OpLog(description = "删除订单", type = 3)
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        TradeRecord record = tradeRecordService.getById(id);
        if (record == null) {
            throw new BusinessException("订单不存在");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new BusinessException("已成交订单不可删除");
        }
        tradeRecordService.removeById(id);
        return Result.success();
    }
}