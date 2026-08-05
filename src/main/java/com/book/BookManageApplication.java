package com.book;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.book.mapper")
public class BookManageApplication {
    private static final Logger log = LoggerFactory.getLogger(BookManageApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BookManageApplication.class, args);
    }
}