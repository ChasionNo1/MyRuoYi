package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.LoginUser;

import java.util.Set;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 公共接口
 * @date 2025/6/15 16:09
 */
public interface IBaseCommonService {

    Set<String> queryUserRoles(String username);

    Set<String> queryUserAuths(String username, String loginType);

    // 根据用户名查询用户信息
    public LoginUser getUserByName(String username);


}