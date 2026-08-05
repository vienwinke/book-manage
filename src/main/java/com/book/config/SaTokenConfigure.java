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
 * 管理类接口需 admin 角色
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 所有接口需登录，以下接口放行
            SaRouter.match("/**")
                    .notMatch("/auth/login", "/auth/register", "/", "/test", "/error")
                    .check(r -> StpUtil.checkLogin());

            // 管理员专属接口（图书/用户管理）
            SaRouter.match("/book/add", "/book/update", "/book/delete/**")
                    .check(r -> StpUtil.checkRole("admin"));
            SaRouter.match("/user/add", "/user/update", "/user/delete/**")
                    .check(r -> StpUtil.checkRole("admin"));
            SaRouter.match("/borrow/approve/**", "/borrow/reject/**", "/borrow/return/**", "/borrow/delete/**")
                    .check(r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }
}
