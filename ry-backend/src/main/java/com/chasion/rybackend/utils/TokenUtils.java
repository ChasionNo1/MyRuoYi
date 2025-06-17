package com.chasion.rybackend.utils;

import com.chasion.rybackend.commons.Constants;
import com.chasion.rybackend.entities.LoginUser;
import com.chasion.rybackend.service.IBaseCommonService;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 封装token工具类
 * @date 2025/6/17 16:46
 */
public class TokenUtils {

    /**
     * 获取登录用户
     * 从redis里取login user
     * 或者通过用户名查询用户信息
     *
     * @param baseCommonService 公共service
     * @param username
     * @return
     */
    public static LoginUser getLoginUser(String username, IBaseCommonService baseCommonService, RedisUtil redisUtil) {
        LoginUser loginUser = null;
        String loginUserKey = Constants.SYS_USERS_CACHE + "::" + username;
        if(redisUtil.hasKey(loginUserKey)){
            loginUser = (LoginUser) redisUtil.get(loginUserKey);
        }else{
            // 查询用户信息
            loginUser = baseCommonService.getUserByName(username);
        }
        return loginUser;
    }
}
