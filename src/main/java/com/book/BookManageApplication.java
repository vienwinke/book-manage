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
        // 先执行计算逻辑
        int num = 7;
        int res = 1;
        for (int i = 1; i <= num; i++) {
            res *= i;
        }
        log.info("{}的阶乘结果：{}",num, res);

        // 只启动一次Spring容器
        SpringApplication.run(BookManageApplication.class, args);
    }
}