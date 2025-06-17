package com.chasion.rybackend.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chasion.rybackend.commons.Constants;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.entities.SysUserRole;
import com.chasion.rybackend.manager.AsyncManager;
import com.chasion.rybackend.manager.factory.AsyncFactory;
import com.chasion.rybackend.mappers.SysUserMapper;
import com.chasion.rybackend.mappers.SysUserRoleMapper;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.ISysUserService;
import com.chasion.rybackend.utils.MessageUtils;
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
            user.setNickname(user.getUsername());
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
            if (!StringUtils.isEmpty(roles)) {
                String[] arr = roles.split(",");
                for (String roleId : arr) {
                    // 添加用户角色
                    SysUserRole userRole = new SysUserRole(user.getUserId(), Long.parseLong(roleId));
                    userRoleMapper.insert(userRole);
                }
            }
        }catch (Exception e) {
            log.error(e.getMessage());
           return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        }
        // 异步添加日志信息
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUsername(), Constants.REGISTER, MessageUtils.message("user.register.success")));
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
    public String login(String username, String password) {
        return "";
    }

    @Override
    public SysUser getUserByUserId(Long userId) {
        return userMapper.selectUserByUserId(userId);
    }

    @Override
    public Result<?> checkUserIsEffective(SysUser user) {
        Result<?> result = new Result<>();
        // user 不存在
        if (user == null) {
            result.error(ResultCode.BAD_REQUEST.getCode(), "该用户不存在，请注册");
//            baseCommonService.addLog("用户登录失败，用户不存在!", Constants.LOG_TYPE_1, null);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUsername(), Constants.USER_IS_NOT_EXIST, MessageUtils.message("user.not.exist")));
            return result;
        }
        // user 已注销
        if (Constants.USER_DEL_FLAG_1.equals(user.getDelFlag())) {
//            baseCommonService.addLog("用户登录失败，用户名:" + user.getUsername() + "已注销!",Constants.LOG_TYPE_1, null);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUsername(), Constants.USER_IS_DEL, MessageUtils.message("user.has.del")));
            result.error(ResultCode.BAD_REQUEST.getCode(), "该用户已注销");
            return result;
        }
        // user 已冻结  0是未冻结，1是冻结
        if (Constants.USER_STATUS_1.equals(user.getStatus())) {
//            baseCommonService.addLog("用户登录失败，用户名:" + user.getUsername() + "已冻结!", Constants.LOG_TYPE_1, null);
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUsername(), Constants.USER_IS_FROZEN, MessageUtils.message("user.has.freeze")));
            result.error(ResultCode.BAD_REQUEST.getCode(), "该用户已冻结");
            return result;
        }

        return result;
    }

    /**
     * 封装用户信息
     * @param user
     * @param result
     * @return
     */
    @Override
    public Result<JSONObject> userInfo(SysUser user, Result<JSONObject> result) {
        String username = user.getUsername();
        String password = user.getPassword();
        JSONObject obj = new JSONObject();
        // 获取部门信息
        Long deptId = user.getDeptId();



        return null;
    }

}
