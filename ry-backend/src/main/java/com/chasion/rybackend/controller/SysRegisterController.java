package com.chasion.rybackend.controller;

import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.SysRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Objects;

/**
 * 注册接口
 * */
@RestController
public class SysRegisterController extends BaseController{
    @Autowired
    private SysRegisterService registerService;

    @PostMapping("/auth/register")
    public Result register(@RequestBody HashMap<String, Object> map) {
        String msg = registerService.register(map);
        if (msg == "注册成功"){
            return Result.success(msg);
        }else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(),msg);
        }
    }


}
