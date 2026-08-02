package com.book;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// 排除数据源自动配置，避开数据库校验
@SpringBootApplication
@MapperScan("com.book.mapper")
public class BookManageApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookManageApplication.class, args);
        System.out.println("hello world");
    }
}