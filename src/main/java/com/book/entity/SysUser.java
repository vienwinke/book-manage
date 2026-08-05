package com.book.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "账号只能包含英文字母和数字，不能使用汉字")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "密码只能使用英文字符、数字和符号，不能包含中文和空格")
    private String password;

    private String realName;

    private Integer userType;

    private Integer status;

    private LocalDateTime createTime;
}