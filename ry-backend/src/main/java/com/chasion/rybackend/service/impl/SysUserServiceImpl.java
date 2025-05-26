package com.chasion.rybackend.service.impl;

import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.mappers.SysUserMapper;
import com.chasion.rybackend.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements ISysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;


    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user)
    {
        return userMapper.insertUser(user) > 0;
    }

    @Override
    public String loginUser(String username, String password) {
        SysUser sysUser = userMapper.selectUserByUsername(username);
        if (sysUser != null) {
            if (sysUser.getPassword().equals(password)) {
                return "登录成功";
            }else {
                return "密码错误";
            }
        }else {
            return "用户不存在";
        }
    }

}
