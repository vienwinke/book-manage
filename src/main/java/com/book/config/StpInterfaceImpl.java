package com.book.config;

import cn.dev33.satoken.stp.StpInterface;
import com.book.entity.SysUser;
import com.book.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 角色数据源：从数据库读取用户类型作为角色
 * user_type 1=管理员 -> 角色 admin；0=学生 -> 角色 student
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roles = new ArrayList<>();
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, Long.valueOf(loginId.toString())));
        if (user != null) {
            roles.add(user.getUserType() != null && user.getUserType() == 1 ? "admin" : "student");
        }
        return roles;
    }
}
