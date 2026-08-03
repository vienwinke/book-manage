package com.book.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 访问地址：http://localhost:8080/
    @GetMapping("/")
    public String index() {
        return "图书管理系统后端服务启动成功！";
    }

    // 额外测试接口：http://localhost:8080/test
    @GetMapping("/test")
    public String test() {
        return "接口测试正常";
    }
}