package com.chasion.rybackend.controller;

import com.chasion.rybackend.entities.SysMenu;
import com.chasion.rybackend.entities.vo.RouterVo;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.service.ISysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 路由权限相关
 * @date 2025/6/19 09:37
 */
@RestController
@RequestMapping("/auth")
public class SysPermissionController {
    @Autowired
    private ISysMenuService menuService;

    // 获取路由信息
    @GetMapping("/getRouters")
    public Result<List<RouterVo>> getRouters() {
        // 获取userId，这里先固定一个，后续通过thread local配合拦截器添加
        // 这里是获取树形的数据，再整形
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(1L);
        return Result.success("获取成功", menuService.buildMenus(menus));

    }
}
