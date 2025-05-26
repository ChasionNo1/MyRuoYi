package com.chasion.rybackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class SysLoginService {

    @Autowired
    private ISysUserService userService;

    // 登录
    public String login(HashMap<String, Object> map){
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String result = userService.loginUser(username, password);
        return result;
    }

}
