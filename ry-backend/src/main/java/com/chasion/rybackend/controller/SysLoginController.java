package com.chasion.rybackend.controller;

import com.chasion.rybackend.entities.SysMenu;
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
import java.util.List;
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
    @GetMapping("/getRouters")
    public Result<List<SysMenu>> getRouters() {
        // 获取userId，这里先固定一个，后续通过thread local配合拦截器添加
        List<SysMenu> menus = menuService.selectMenuList(1L);
        return Result.success("获取成功", menuService.buildMenuTree(menus));

    }
}
