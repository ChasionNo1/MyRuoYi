package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.SysUser;

/**
 * 用户业务层
 * */
public interface ISysUserService {

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public boolean registerUser(SysUser user);

    // 登录
    public String loginUser(String username, String password);
}
