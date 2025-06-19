package com.chasion.rybackend.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chasion.rybackend.commons.Constants;
import com.chasion.rybackend.entities.LoginUser;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.exception.BusinessException;
import com.chasion.rybackend.manager.AsyncManager;
import com.chasion.rybackend.manager.factory.AsyncFactory;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.ISysUserService;
import com.chasion.rybackend.utils.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 登录接口
 * @date 2025/6/15 15:37
 */
@RestController
@Tag(name = "用户登录接口")
@RequestMapping("/sys")
@Slf4j
public class LoginController {
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * @description: 登录接口，未开启验证码
     * @param: username password
     * @returns: com.chasion.rybackend.resp.Result
     * @author chasion
     * @date: 2025/6/15 15:52
     */
    @PostMapping("/auth/login")
    @Operation(summary = "用户名密码登录接口，未带验证功能")
    public Result<String> login(@RequestBody LoginUser loginUser){
        // 1、先从数据库根据username查
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, loginUser.getUsername());
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        // 2、查出来的user再进行校验用户是否有效
        Result result = sysUserService.checkUserIsEffective(sysUser);
        // 这里应该判断失败情况
        if (ResultCode.BAD_REQUEST.getCode().equals(result.getCode())) {
            return result;
        }
        // 3、校验用户密码是否正确
        String encrypt = PasswordUtil.encrypt(loginUser.getUsername(), loginUser.getPassword(), sysUser.getSalt());
        String save = sysUser.getPassword();
        if (!save.equals(encrypt)) {
            result.error(ResultCode.BAD_REQUEST.getCode(), "用户名或密码错误");
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginUser.getUsername(), Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            return result;
        }
        // 更新用户信息
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtil.date());
        sysUserService.updateUserInfo(sysUser);
        // 生成token
        // 这里是设置的地方，为何还要new一个呢？登录的时候传递的是用户名和密码
        BeanUtils.copyProperties(sysUser, loginUser);
        // 通过
        String token = JwtUtils.sign(loginUser, "pc", loginUser.getPassword());
        // 设置token缓存有效时间
        redisUtil.set(Constants.PREFIX_USER_TOKEN + token, token);
        redisUtil.expire(Constants.PREFIX_USER_TOKEN + token, JwtUtils.EXPIRE_TIME * 2 / 1000);
        // 登录成功
        // 这里要设置一下token，后面可以直接从loginUser获取
        loginUser.setToken(token);
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginUser.getUsername(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        return new Result<>(ResultCode.SUCCESS.getCode(), "登录成功", token);
    }

    @PostMapping("/auth/logout")
    @Operation(summary = "用户退出登录接口")
    public Result logout(){
        // 获取当前用户
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        try {
            // 删除缓存
            redisUtil.del(Constants.PREFIX_USER_TOKEN + loginUser.getToken());
            // 清除认证
            SecurityUtils.getSubject().logout();
        }catch (Exception e) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "退出登录异常");
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginUser.getUsername(), Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        return new Result<>(ResultCode.SUCCESS.getCode(), "退出登录成功");

    }




}