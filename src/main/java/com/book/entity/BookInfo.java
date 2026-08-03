package com.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("book_info")
public class BookInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bookName;

    private String author;

    private String isbn;

    private String category;

    private String version;

    private String quality;

    private Integer totalNum;

    private Integer availableNum;

    private Integer bookStatus;

    private String source;

    private String remark;

    private LocalDateTime createTime;
}