package com.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("borrow_record")
public class BorrowRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "借阅人不能为空")
    private Long userId;

    @NotNull(message = "图书不能为空")
    private Long bookId;

    private LocalDateTime applyTime;

    private LocalDate expectReturnTime;

    private LocalDateTime actualReturnTime;

    @TableField("record_status")
    private Integer status;

    private String adminRemark;
}