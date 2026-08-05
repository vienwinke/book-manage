# book-manage 二手图书借阅管理系统

基于 Spring Boot 3 的二手图书借阅管理系统后端项目。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 开发语言 |
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM 持久层（含分页插件） |
| Sa-Token | 1.38.0 | 认证授权（JWT token，角色控制） |
| MySQL | 8.0 | 数据库 |
| Hutool | 5.8.27 | 工具类库（BCrypt 加密） |
| Spring AOP | - | 操作日志切面 |
| Validation / Actuator | - | 参数校验、监控 |

## 项目结构

```
book-manage
├── pom.xml                        # Maven 配置
├── sql/book_db.sql                # 建表脚本 + 初始化数据（密码统一为 123456）
└── src/main
    ├── resources
    │   ├── application.yml        # 数据源 / MyBatis-Plus / Sa-Token 配置
    │   └── test.http              # 接口测试请求
    └── java/com/book
        ├── BookManageApplication.java   # 启动类
        ├── config/                # SaTokenConfigure 拦截器、StpInterface 角色、MybatisPlusConfig 分页
        ├── annotation/ + aspect/  # @OpLog 操作日志注解与切面
        ├── controller/            # Auth、Book、Borrow、User、Log 控制器
        ├── service/ + impl/       # 业务层
        ├── mapper/                # MyBatis-Plus Mapper
        ├── entity/                # 4 个实体
        ├── util/                  # Result、ResultCode
        └── exception/             # GlobalExceptionHandler、BusinessException
```

## 数据表（4 张，详见 sql/book_db.sql）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_user` | 用户表 | username(唯一)、password(BCrypt)、realName、userType(0学生/1管理员)、status |
| `book_info` | 图书表 | bookName、author、isbn、category、totalNum、availableNum、bookStatus |
| `borrow_record` | 借阅记录表 | userId、bookId、recordStatus(0待审/1借出/2已还/3拒绝)、expectReturnTime |
| `sys_log` | 操作日志表 | userId、logType、description、ip、operateTime |

## API 接口

所有接口返回 `Result<T>`：`{code, msg, data}`，code=200 成功、400 参数错误、401 未登录、403 无权限、600 业务异常。

**统一鉴权：** 除 `/auth/login`、`/auth/register`、`/`、`/test` 外均需登录（请求头 `satoken: <token>`）；图书/用户管理、借阅审核接口需管理员角色。

### 认证 `/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 登录（body: username, password），返回 token + 用户信息 |
| POST | /auth/register | 学生注册（自动加密、角色固定为学生） |
| POST | /auth/logout | 退出登录 |
| GET | /auth/info | 当前登录用户信息 |

### 图书 `/book`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /book/page | 分页查询（bookName/author/category 模糊搜索，bookStatus 筛选） |
| GET | /book/{id} | 按 ID 查询 |
| POST | /book/add | 新增图书（管理员） |
| PUT | /book/update | 修改图书（管理员） |
| DELETE | /book/delete/{id} | 删除图书（管理员） |

### 借阅 `/borrow`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /borrow/page | 分页查询（userId/status 筛选） |
| GET | /borrow/{id} | 按 ID 查询 |
| POST | /borrow/apply | 学生申请借阅（校验库存，同人不可并发未完结借阅） |
| POST | /borrow/approve/{id} | 管理员审核通过，库存减一 |
| POST | /borrow/reject/{id} | 管理员拒绝 |
| POST | /borrow/return/{id} | 归还图书，库存加一 |
| DELETE | /borrow/delete/{id} | 删除记录（管理员） |

### 用户 `/user`（管理员）
分页/按ID查询/新增/修改/删除，写入接口自动记录日志。

### 日志 `/log`
GET /log/page、GET /log/{id}、DELETE /log/delete/{id}。写操作由 AOP 自动记录（@OpLog）。

## 环境配置

- 数据库：`book_db`，连接地址见 `application.yml`（WSL 中运行需用 Windows 主机 IP）
- 账号密码支持环境变量：`DB_USERNAME`、`DB_PASSWORD`
- Sa-Token token 有效期 30 天

## 快速开始

```bash
# 1. 初始化数据库（在 MySQL 中执行 sql/book_db.sql）
# 2. 确认 application.yml 数据库地址与账号
mvn spring-boot:run
```

初始账号（密码均为 `123456`）：
- `admin` — 系统管理员（userType=1）
- `stu01` / `stu02` — 学生（userType=0）

登录示例：
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
# 返回 token 后，后续请求携带请求头 satoken: <token>
```
