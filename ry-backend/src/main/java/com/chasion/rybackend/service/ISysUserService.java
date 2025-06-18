package com.chasion.rybackend.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.resp.Result;

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
    public String login(String username, String password);

    SysUser getUserByUserId(Long userId);

    Result checkUserIsEffective(SysUser user);

    Result<JSONObject> userInfo(SysUser user, Result<JSONObject> result);

    public int updateUserInfo(SysUser user);
}
