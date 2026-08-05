package com.book.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解：标注在需要记录日志的接口方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /** 操作描述 */
    String description() default "";

    /** 日志类型：0-查询 1-新增 2-修改 3-删除 4-登录 5-其他 */
    int type() default 5;
}
