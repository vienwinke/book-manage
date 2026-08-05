package com.book.service;

import com.book.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysUserService extends IService<SysUser> {

    /**
     * 登录校验，返回用户信息（不含密码）
     */
    SysUser login(String username, String password);

    /**
     * 学生注册（密码加密、类型固定为学生）
     */
    void register(SysUser user);
}
