package com.book.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.book.annotation.OpLog;
import com.book.entity.SysLog;
import com.book.service.SysLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面：拦截 @OpLog 注解方法，自动写入 sys_log 表
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            saveLog(opLog, System.currentTimeMillis() - start);
        }
    }

    private void saveLog(OpLog opLog, long cost) {
        try {
            SysLog sysLog = new SysLog();
            sysLog.setUserId(getLoginId());
            sysLog.setLogType(opLog.type());
            sysLog.setDescription(opLog.description() + "（耗时 " + cost + "ms）");
            sysLog.setIp(getIp());
            sysLog.setOperateTime(LocalDateTime.now());
            sysLogService.save(sysLog);
        } catch (Exception e) {
            log.warn("记录操作日志失败: {}", e.getMessage());
        }
    }

    private Long getLoginId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null; // 未登录场景（如登录接口本身）
        }
    }

    private String getIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            int commaIndex = ip.indexOf(',');
            ip = commaIndex > 0 ? ip.substring(0, commaIndex).trim() : ip.trim();
            if (isValidIp(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }
        return ip.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$")
                || ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
    }
}
