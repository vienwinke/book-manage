package com.book.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // 访问地址：http://localhost:8080/  → 仿前端风格的网页欢迎页
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>二手书买卖管理系统</title>
                  <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Microsoft YaHei", sans-serif;
                      background: linear-gradient(135deg, #f5f7fa 0%, #eef2f7 100%);
                      min-height: 100vh;
                      color: #303133;
                    }
                    .header {
                      background: #409EFF;
                      padding: 20px 24px;
                      color: #fff;
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
                    }
                    .header .title { font-size: 20px; font-weight: 600; }
                    .header .badge {
                      background: rgba(255,255,255,0.2);
                      padding: 4px 12px;
                      border-radius: 999px;
                      font-size: 12px;
                    }
                    .container { max-width: 960px; margin: 0 auto; padding: 32px 20px; }
                    .hero {
                      background: #fff;
                      border-radius: 12px;
                      padding: 28px;
                      text-align: center;
                      box-shadow: 0 2px 12px rgba(0,0,0,0.06);
                      margin-bottom: 24px;
                    }
                    .hero h1 { font-size: 26px; color: #303133; margin-bottom: 10px; }
                    .hero p { color: #909399; font-size: 14px; line-height: 1.8; }
                    .hero .status {
                      display: inline-block;
                      margin-top: 14px;
                      padding: 6px 18px;
                      border-radius: 999px;
                      background: #f0f9eb;
                      color: #67c23a;
                      font-size: 13px;
                      font-weight: 500;
                    }
                    .section-title {
                      font-size: 16px;
                      font-weight: 600;
                      color: #303133;
                      margin: 24px 0 14px;
                      display: flex;
                      align-items: center;
                      gap: 8px;
                    }
                    .section-title::before {
                      content: '';
                      width: 4px;
                      height: 16px;
                      background: #409EFF;
                      border-radius: 2px;
                    }
                    .cards {
                      display: grid;
                      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
                      gap: 14px;
                    }
                    .card {
                      background: #fff;
                      border-radius: 10px;
                      padding: 18px;
                      border: 1px solid #ebeef5;
                      transition: all 0.2s;
                      cursor: default;
                    }
                    .card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); }
                    .card .icon { font-size: 26px; margin-bottom: 8px; }
                    .card .name { font-size: 15px; font-weight: 600; color: #303133; margin-bottom: 6px; }
                    .card .desc { font-size: 12px; color: #909399; line-height: 1.6; }
                    .tech {
                      background: #fff;
                      border-radius: 10px;
                      padding: 18px 20px;
                      border: 1px solid #ebeef5;
                    }
                    .tech .tag {
                      display: inline-block;
                      margin: 4px 6px 4px 0;
                      padding: 4px 12px;
                      border-radius: 4px;
                      background: #ecf5ff;
                      color: #409EFF;
                      font-size: 12px;
                    }
                    .entry {
                      display: flex;
                      gap: 12px;
                      margin-top: 24px;
                      flex-wrap: wrap;
                    }
                    .btn {
                      display: inline-block;
                      padding: 10px 22px;
                      border-radius: 6px;
                      font-size: 14px;
                      text-decoration: none;
                      transition: all 0.2s;
                    }
                    .btn-primary { background: #409EFF; color: #fff; }
                    .btn-primary:hover { background: #66b1ff; }
                    .btn-plain { background: #fff; color: #409EFF; border: 1px solid #409EFF; }
                    .btn-plain:hover { background: #ecf5ff; }
                    .footer {
                      text-align: center;
                      color: #c0c4cc;
                      font-size: 12px;
                      padding: 32px 0 16px;
                    }
                  </style>
                </head>
                <body>
                  <div class="header">
                    <div class="title">📚 二手书买卖管理系统</div>
                    <span class="badge">后端服务</span>
                  </div>
                  <div class="container">
                    <div class="hero">
                      <h1>欢迎使用二手书买卖管理系统</h1>
                      <p>基于 Spring Boot 3 + MyBatis-Plus + Sa-Token 的图书交易平台<br>提供图书上架、下单购买、订单确认、用户与日志管理等完整能力</p>
                      <span class="status">● 后端服务运行正常</span>
                    </div>

                    <div class="section-title">核心功能</div>
                    <div class="cards">
                      <div class="card"><div class="icon">📖</div><div class="name">图书浏览</div><div class="desc">分页检索在售图书，支持书名/作者/分类模糊搜索</div></div>
                      <div class="card"><div class="icon">🛒</div><div class="name">下单购买</div><div class="desc">一键下单，卖家确认成交，库存自动扣减</div></div>
                      <div class="card"><div class="icon">📷</div><div class="name">扫码上架</div><div class="desc">摄像头扫描 ISBN 自动识别，快速录入图书</div></div>
                      <div class="card"><div class="icon">📦</div><div class="name">订单管理</div><div class="desc">确认成交 / 拒绝 / 取消，状态全程可追溯</div></div>
                      <div class="card"><div class="icon">👥</div><div class="name">用户管理</div><div class="desc">管理员维护用户，角色权限严格隔离</div></div>
                      <div class="card"><div class="icon">📜</div><div class="name">操作日志</div><div class="desc">AOP 自动记录所有关键操作，支持审计</div></div>
                    </div>

                    <div class="section-title">技术栈</div>
                    <div class="tech">
                      <span class="tag">Java 21</span>
                      <span class="tag">Spring Boot 3.2</span>
                      <span class="tag">MyBatis-Plus</span>
                      <span class="tag">Sa-Token</span>
                      <span class="tag">MySQL 8.0</span>
                      <span class="tag">Vue 3 + Element Plus</span>
                    </div>

                    <div class="entry">
                      <a class="btn btn-primary" href="http://localhost:5173" target="_blank">进入 Web 管理端</a>
                      <a class="btn btn-plain" href="/book/page?pageNum=1&pageSize=5" target="_blank">查看图书数据</a>
                      <a class="btn btn-plain" href="/actuator/health" target="_blank">服务健康检查</a>
                    </div>

                    <div class="footer">book-manage · Spring Boot 3.2.5 · 后端服务地址 127.0.0.1:8080</div>
                  </div>
                </body>
                </html>
                """;
    }

    // 额外测试接口：http://localhost:8080/test
    @GetMapping("/test")
    public String test() {
        return "接口测试正常";
    }
}