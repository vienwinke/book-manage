package com.book.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败锁定 + 注册频率限制（基于内存实现）。
 * 登录：同一 IP + 用户名，连续失败 5 次锁定 15 分钟。
 * 注册：同一 IP，10 分钟内最多注册 3 个账号。
 * 注意：多实例部署时需改为 Redis 等共享存储。
 */
@Component
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L; // 15 分钟

    private static final int MAX_REGISTERS = 3;
    private static final long REGISTER_WINDOW_MS = 10 * 60 * 1000L; // 10 分钟

    private static class AttemptInfo {
        int count;
        long lockUntil; // 0 表示未锁定
    }

    private final Map<String, AttemptInfo> failures = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> registerTimes = new ConcurrentHashMap<>();

    /** 当前是否处于锁定状态 */
    public boolean isLocked(String key) {
        AttemptInfo info = failures.get(key);
        if (info == null) {
            return false;
        }
        if (info.lockUntil > System.currentTimeMillis()) {
            return true;
        }
        if (info.lockUntil > 0) {
            failures.remove(key); // 锁定期已过，清除记录
        }
        return false;
    }

    /** 记录一次失败 */
    public void recordFailure(String key) {
        AttemptInfo info = failures.computeIfAbsent(key, k -> new AttemptInfo());
        info.count++;
        if (info.count >= MAX_FAILURES) {
            info.lockUntil = System.currentTimeMillis() + LOCK_DURATION_MS;
        }
    }

    /** 登录成功后清除失败记录 */
    public void clear(String key) {
        failures.remove(key);
    }

    /** 剩余锁定秒数 */
    public long getLockRemainingSeconds(String key) {
        AttemptInfo info = failures.get(key);
        if (info != null && info.lockUntil > System.currentTimeMillis()) {
            return (info.lockUntil - System.currentTimeMillis()) / 1000;
        }
        return 0;
    }

    /** 是否允许注册（按 IP 限流），通过则记录本次注册时间 */
    public boolean allowRegister(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> times = registerTimes.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            while (!times.isEmpty() && now - times.peekFirst() > REGISTER_WINDOW_MS) {
                times.pollFirst();
            }
            if (times.size() >= MAX_REGISTERS) {
                return false;
            }
            times.addLast(now);
            return true;
        }
    }
}