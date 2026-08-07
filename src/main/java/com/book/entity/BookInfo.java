package com.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("book_info")
public class BookInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "书名不能为空")
    private String bookName;

    private String author;

    private String isbn;

    private String category;

    private String version;

    private String quality;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private Long sellerId;

    private String sellerName;

    /** 状态 0-在售 1-已售 2-下架 */
    private Integer bookStatus;

    private String remark;

    private LocalDateTime createTime;
}