package com.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
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

    @Min(value = 0, message = "总数量不能小于0")
    private Integer totalNum;

    @Min(value = 0, message = "可借数量不能小于0")
    private Integer availableNum;

    private Integer bookStatus;

    private String source;

    private String remark;

    private LocalDateTime createTime;
}