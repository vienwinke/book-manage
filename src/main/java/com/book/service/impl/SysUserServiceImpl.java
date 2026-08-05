package com.book.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book.entity.SysUser;
import com.book.exception.BusinessException;
import com.book.mapper.SysUserMapper;
import com.book.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(String username, String password) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void register(SysUser user) {
        Long count = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setUserType(0); // 注册用户默认为学生
        user.setStatus(1);
        save(user);
    }
}
