package com.chasion.rybackend.controller;

import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.ISysMenuService;
import com.chasion.rybackend.service.SysLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Objects;

@RestController
public class SysLoginController {

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @PostMapping("/auth/login")
    public Result login(@RequestBody HashMap<String, Object> map){
        String res = loginService.login(map);
        if ("登录成功".equals(res)){
            return Result.success(res);
        }else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), res);
        }
    }

    // 获取路由信息
    @GetMapping("/getRouters"){
        // 获取userId

    }
}
