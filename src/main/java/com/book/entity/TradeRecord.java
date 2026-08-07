package com.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trade_record")
public class TradeRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "图书不能为空")
    private Long bookId;

    private String bookName;

    private BigDecimal orderPrice;

    private Long sellerId;

    private String sellerName;

    @NotNull(message = "买家不能为空")
    private Long buyerId;

    private String buyerName;

    /** 状态 0-待确认 1-已成交 2-已取消 3-已拒绝 */
    @TableField("status")
    private Integer status;

    private LocalDateTime applyTime;

    private LocalDateTime doneTime;

    private String remark;
}