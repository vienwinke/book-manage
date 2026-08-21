package com.book.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置：除登录/注册等公开接口外，其余接口需登录
 * 管理类接口（图书/用户管理、订单确认成交/拒绝/删除）需 admin 角色
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 注意: token 通过请求头 satoken 传递，不依赖 Cookie，
        // 因此 allowCredentials 保持 false，避免“任意来源 + 携带凭证”的高危组合。
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 所有接口需登录，以下接口放行
            SaRouter.match("/**")
                    .notMatch("/auth/login", "/auth/register", "/", "/error")
                    .check(r -> StpUtil.checkLogin());

            // 用户管理、日志管理（含查询/删除）全部需 admin 角色
            // 图书修改/删除、订单确认成交/拒绝 由 controller 内校验「卖家本人或管理员」
            SaRouter.match("/user/**", "/log/**")
                    .check(r -> StpUtil.checkRole("admin"));
            SaRouter.match("/order/delete/**")
                    .check(r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }
}