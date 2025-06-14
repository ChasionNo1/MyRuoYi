package com.chasion.rybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.resp.Result;

import java.util.HashMap;

/**
 * 用户业务层
 * */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public Result registerUserWithRole(SysUser user, String roles);

    SysUser getUserByUsername(String username);

    SysUser getUserByEmail(String email);

    // 登录
    public String loginUser(String username, String password);

    SysUser getUserByUserId(Long userId);
}
