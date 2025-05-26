package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * 注册校验方法
 *
 * */
@Service
public class SysRegisterService {
    @Autowired
    private ISysUserService userService;

    /**
     * 注册
     */
    public String register(HashMap<String, Object> map) {
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        // 创建用户
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setNickname(username);
        sysUser.setPwdUpdateDate(new Date());
        sysUser.setPassword(password);
//        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
//        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        boolean regFlag = userService.registerUser(sysUser);
        String msg = "";
        if (regFlag) {
            msg = "注册成功";
        }else {
            msg = "注册失败";
        }
        return msg;
    }

}
