package com.chasion.rybackend.controller;

import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.SysRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Objects;

/**
 * 注册接口
 * */
@RestController
public class SysRegisterController extends BaseController{
    @Autowired
    private SysRegisterService registerService;

    // 响应注册请求
    @PostMapping("/auth/register")
    public Result register(@RequestBody HashMap<String, Object> map) {
        // 判断是否为空
        // 校验验证码
        // 两次密码不一致
        // 调用service
        String msg = registerService.register(map);
        if (msg == "注册成功"){
            return Result.success(msg);
        }else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(),msg);
        }
    }

    // 响应邮箱验证码
    @GetMapping("/auth/verify/email")
    public Result<String> sendEmailCode(@RequestParam("email") String email) {
        return Result.success("发送成功", "1111");
    }



}
