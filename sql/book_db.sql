-- ============================================================
-- book-manage 二手图书借阅管理系统 数据库初始化脚本
-- 适用：MySQL 8.0+
-- 初始密码说明：以下用户密码均为 BCrypt 加密，明文为 123456
-- ============================================================

CREATE DATABASE IF NOT EXISTS book_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE book_db;

-- 忽略外键约束，允许重建表
SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    real_name   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    user_type   TINYINT      NOT NULL DEFAULT 0 COMMENT '用户类型 0-学生 1-管理员',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='系统用户表';

-- ------------------------------------------------------------
-- 2. 图书信息表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS book_info;
CREATE TABLE book_info (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    book_name     VARCHAR(128) NOT NULL COMMENT '书名',
    author        VARCHAR(64)  DEFAULT NULL COMMENT '作者',
    isbn          VARCHAR(32)  DEFAULT NULL COMMENT 'ISBN',
    category      VARCHAR(32)  DEFAULT NULL COMMENT '分类',
    version       VARCHAR(32)  DEFAULT NULL COMMENT '版次',
    quality       VARCHAR(32)  DEFAULT NULL COMMENT '成色',
    total_num     INT          NOT NULL DEFAULT 0 COMMENT '总数量',
    available_num INT          NOT NULL DEFAULT 0 COMMENT '可借数量',
    book_status   TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-上架 1-下架',
    source        VARCHAR(32)  DEFAULT NULL COMMENT '来源',
    remark        VARCHAR(256) DEFAULT NULL COMMENT '备注',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='图书信息表';

-- ------------------------------------------------------------
-- 3. 借阅记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS borrow_record;
CREATE TABLE borrow_record (
    id                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id            BIGINT       NOT NULL COMMENT '借阅人ID',
    book_id            BIGINT       NOT NULL COMMENT '图书ID',
    apply_time         DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    expect_return_time DATE         DEFAULT NULL COMMENT '预计归还日期',
    actual_return_time DATETIME     DEFAULT NULL COMMENT '实际归还时间',
    record_status      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-待审核 1-借出 2-已归还 3-已拒绝',
    admin_remark       VARCHAR(256) DEFAULT NULL COMMENT '管理员备注',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_book_id (book_id)
) ENGINE=InnoDB COMMENT='借阅记录表';

-- ------------------------------------------------------------
-- 4. 操作日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id      BIGINT       DEFAULT NULL COMMENT '操作人ID',
    log_type     TINYINT      DEFAULT NULL COMMENT '日志类型 0-查询 1-新增 2-修改 3-删除 4-登录 5-其他',
    description  VARCHAR(256) DEFAULT NULL COMMENT '操作描述',
    ip           VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
    operate_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ------------------------------------------------------------
-- 初始化数据
-- ------------------------------------------------------------
-- 用户（密码明文均为 123456）
INSERT INTO sys_user (username, password, real_name, user_type, status) VALUES
('admin', '$2a$10$1eAZNW4tuAOmtzpHsg8fieWhYSLwF7.IoshRuB6FXg5pcBcCAF/.2', '系统管理员', 1, 1),
('stu01', '$2a$10$1eAZNW4tuAOmtzpHsg8fieWhYSLwF7.IoshRuB6FXg5pcBcCAF/.2', '张三', 0, 1),
('stu02', '$2a$10$1eAZNW4tuAOmtzpHsg8fieWhYSLwF7.IoshRuB6FXg5pcBcCAF/.2', '李四', 0, 1);

-- 图书
INSERT INTO book_info (book_name, author, isbn, category, version, quality, total_num, available_num, book_status, source, remark) VALUES
('Java编程思想', 'Bruce Eckel', '9787111213826', '计算机', '第4版', '轻微磨损', 3, 3, 0, '学生捐赠', '少量划线笔记'),
('高等数学上册', '同济大学', '9787040091314', '教材', '第七版', '较旧', 5, 5, 0, '社团采购', '无缺页'),
('三体', '刘慈欣', '9787536692930', '小说', '1版', '全新', 2, 2, 0, '学生捐赠', '无笔记'),
('百年孤独', '马尔克斯', '9787544269982', '文学', '新版', '破损', 1, 1, 0, '学生捐赠', '封底撕裂'),
('数据结构与算法', '严蔚敏', '9787302147510', '计算机', 'C语言版', '轻微磨损', 2, 2, 0, '社团采购', NULL);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
