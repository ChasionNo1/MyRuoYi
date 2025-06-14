package com.chasion.rybackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chasion.rybackend.commons.Constants;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.entities.SysUserRole;
import com.chasion.rybackend.mappers.SysUserMapper;
import com.chasion.rybackend.mappers.SysUserRoleMapper;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.ISysUserService;
import com.chasion.rybackend.utils.PasswordUtil;
import com.chasion.rybackend.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;


    /**
     * @description: 注册方法，这里应该用mybatis plus的
     * @param: map
     * @returns: java.lang.String
     * @author chasion
     * @date: 2025/6/14 20:10
     */
    @Override
    public Result registerUserWithRole(SysUser user, String roles) {
        // 先来判断一下，在controller里已经判断了不为空、验证码的问题
        // 判断用户是否注册过
        SysUser tmpUser = null;
        tmpUser = getUserByUsername(user.getUsername());
        if (tmpUser != null) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "用户已存在");
        }
        tmpUser = getUserByEmail(user.getEmail());
        if (tmpUser != null) {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), "用户已存在");
        }
        try {
            // 填充数据
            user.setCreateTime(new Date());
            String salt = StringUtils.randomGen(8);
            // 密码加密
            String encrypt = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
            user.setPassword(encrypt);
            user.setSalt(salt);
            // 定义状态0是有效
            user.setStatus(Constants.USER_STATUS_0);
            user.setDelFlag(Constants.USER_DEL_FLAG_0);
            this.save(user);
            if (StringUtils.isEmpty(roles)) {
                String[] arr = roles.split(",");
                for (String roleId : arr) {
                    // 添加用户角色
                    SysUserRole userRole = new SysUserRole(user.getUserId(), Long.parseLong(roleId));
                    userRoleMapper.insert(userRole);
                }
            }
        }catch (Exception e) {
           return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        }
        return Result.success("注册成功");
    }

    @Override
    public SysUser getUserByUsername(String username) {
        return userMapper.selectUserByUsername(username);
    }

    @Override
    public SysUser getUserByEmail(String email) {
        return userMapper.selectUserByEmail(email);
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

    @Override
    public SysUser getUserByUserId(Long userId) {
        return userMapper.selectUserByUserId(userId);
    }

}
