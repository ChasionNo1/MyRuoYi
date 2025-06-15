package com.chasion.rybackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    /**
     * @description: 登录接口，未开启验证码
     * @param: username password
     * @returns: com.chasion.rybackend.resp.Result
     * @author chasion
     * @date: 2025/6/15 15:52
     */
    @PostMapping("/auth/login")
    @Operation(summary = "用户名密码登录接口，未带验证功能")
    public Result login(String username, String password){
        // 1、先从数据库根据username查
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserService.getOne(queryWrapper);
        // 2、查出来的user再进行校验用户是否有效


    }


}