# book-manage 二手书买卖管理系统

基于 **Spring Boot 3** 的二手书买卖管理系统。提供图书上架、下单购买、订单确认、用户管理、操作日志等功能，纯后端 API 服务（暂不涉及付款通道）。

## 代码作用（各模块职责）

| 模块/包 | 作用 |
|---------|------|
| `controller/` | 接口层：认证、图书、订单、用户、日志 5 组 REST API |
| `service/ + impl/` | 业务逻辑层（下单、成交、库存扣减、登录等） |
| `mapper/` | MyBatis-Plus 数据访问层 |
| `entity/` | 数据库实体（用户/图书/订单/日志） |
| `config/` | Sa-Token 拦截器与角色源、MyBatis-Plus 分页配置 |
| `annotation/ + aspect/` | `@OpLog` 操作日志注解与 AOP 切面（自动记录日志） |
| `exception/` | 业务异常与全局异常处理 |
| `util/` | 统一响应 `Result<T>` 与状态码 |
| `sql/book_db.sql` | 建表脚本 + 初始化数据 |
| `miniprogram/` | 微信小程序（扫码识别 + 买卖，纯前端演示） |
| `scan.html` | 网页版扫码（html5-qrcode） |

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 开发语言 |
| Spring Boot | 3.2.5 | 基础框架 |
| MyBatis-Plus | 3.5.5 | ORM 持久层（含分页插件） |
| Sa-Token | 1.38.0 | 认证授权（token，角色控制） |
| MySQL | 8.0 | 数据库 |
| Hutool | 5.8.27 | 工具类库（BCrypt 加密） |
| Spring AOP | - | 操作日志切面 |
| Validation / Actuator | - | 参数校验、监控 |

## 环境要求

- **JDK 21+**（`pom.xml` 中 `java.version=21`）
- **Maven 3.8+**
- **MySQL 8.0+**（本地或可访问的实例）
- 运行于 Linux / WSL / Windows 均可

## 快速开始

```bash
# 1. 初始化数据库（在 MySQL 中执行）
mysql -u root -p < sql/book_db.sql

# 2. 配置数据库连接（必须通过环境变量注入，无硬编码默认密码）
export DB_USERNAME=root        # 可选，默认 root
export DB_PASSWORD=你的数据库密码   # 必填，无默认值

# 3. 启动
mvn spring-boot:run
```

> 提示：`application.yml` 中数据库地址默认 `127.0.0.1:3306`。若 MySQL 运行在 Windows 宿主机而代码在 WSL 中运行，请将地址改为 Windows 主机 IP。

## 初始账号

所有初始账号密码均为 `123456`（BCrypt 加密存储）：

| 账号 | 类型 | 说明 |
|------|------|------|
| `admin` | 管理员 | 可管理用户/日志/订单，可确认成交 |
| `stu01` | 普通用户 | 学生，可上架/购买图书 |
| `stu02` | 普通用户 | 学生 |

> ⚠️ **部署到公网前务必修改默认密码**，或删除初始化用户。

## 数据表（4 张）

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_user` | 用户表 | username(唯一)、password(BCrypt)、userType(0普通/1管理员)、status(0禁用/1启用) |
| `book_info` | 图书表（在售条目） | bookName、price、stock、sellerId/sellerName、bookStatus(0在售/1已售/2下架) |
| `trade_record` | 交易订单表 | bookId、buyerId/sellerId、orderPrice、status(0待确认/1已成交/2已取消/3已拒绝) |
| `sys_log` | 操作日志表 | userId、logType、description、ip、operateTime |

## API 接口

所有接口返回 `Result<T>`：`{code, msg, data}`，code=200 成功、400 参数错误、401 未登录、403 无权限、600 业务异常。

**鉴权规则：**
- 除 `/auth/login`、`/auth/register`、`/`、`/error` 外，所有接口需登录（请求头 `satoken: <token>`）
- `/user/**`、`/log/**`、`/order/delete/**` 仅限 **admin** 角色
- 图书修改/删除、订单确认成交/拒绝：**卖家本人或管理员**（controller 内校验）
- 订单查看：普通用户只能查看自己相关订单

### 认证 `/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 登录（连续失败 5 次锁定 15 分钟） |
| POST | /auth/register | 普通用户注册（同一 IP 10 分钟内最多 3 次） |
| POST | /auth/logout | 退出登录 |
| GET | /auth/info | 当前登录用户信息 |

### 图书 `/book`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /book/page | 分页查询（书名/作者/分类模糊、ISBN/卖家/状态精确，单页 ≤50） |
| GET | /book/{id} | 按 ID 查询 |
| POST | /book/add | 上架图书（售价必须 >0，自动记录当前登录者为卖家） |
| PUT | /book/update | 修改图书（卖家本人或管理员；所有权字段不可篡改） |
| DELETE | /book/delete/{id} | 删除图书（存在待确认订单时禁止） |

### 订单 `/order`（暂不涉及付款）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /order/page | 分页查询（普通用户仅见自己的订单） |
| GET | /order/{id} | 按 ID 查询（仅本人相关或管理员） |
| POST | /order/apply | 下单（buyerId 取当前登录用户；行锁防并发重复下单） |
| POST | /order/confirm/{id} | 卖家/管理员确认成交（行锁扣库存） |
| POST | /order/reject/{id} | 卖家/管理员拒绝订单 |
| POST | /order/cancel/{id} | 买家本人或管理员取消 |
| DELETE | /order/delete/{id} | 管理员删除（已成交订单不可删） |

### 用户 `/user`（仅 admin）
分页/按 ID 查询/新增/修改/删除。新增与修改的密码均自动 BCrypt 加密；删除或禁用用户会立即注销其 token。

### 日志 `/log`（仅 admin）
`/log/page`、`/log/{id}`、`/log/delete/{id}`。写操作由 AOP 自动记录（`@OpLog`）。

## 安全设计说明

本轮已修复的安全问题与防御措施：

- **越权访问**：`/user/**`、`/log/**` 全部接口限定 admin 角色
- **明文密码**：修改用户时密码 BCrypt 加密后入库；不传则不更新
- **删除用户 token 残留**：删除/禁用用户时立即注销其会话
- **并发下单/成交**：对图书记录加 `SELECT ... FOR UPDATE` 行锁
- **登录爆破**：失败 5 次锁定 15 分钟；注册按 IP 限流
- **CORS**：`allowCredentials(false)`（token 走请求头，不依赖 Cookie）
- **数据库密码**：必须通过环境变量 `DB_PASSWORD` 注入，无硬编码默认值
- **参数校验**：售价 >0、分页大小上限 50、SQL 字符串全部经参数化/转义

## 登录示例

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
# 返回 token 后，后续请求携带请求头 satoken: <token>
```