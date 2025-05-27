package com.chasion.rybackend.controller;

import com.chasion.rybackend.entities.SysMenu;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.entities.vo.RouterVo;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.resp.ResultCode;
import com.chasion.rybackend.service.ISysMenuService;
import com.chasion.rybackend.service.ISysUserService;
import com.chasion.rybackend.service.SysLoginService;
import com.chasion.rybackend.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
public class SysLoginController {

    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysUserService userService;

    @PostMapping("/auth/login")
    public Result login(@RequestBody HashMap<String, Object> map){
        String res = loginService.login(map);
        if ("登录成功".equals(res)){
            return Result.success(res,"aaaabbbbtoken");
        }else {
            return Result.error(ResultCode.BAD_REQUEST.getCode(), res);
        }
    }

    // 获取路由信息
    @GetMapping("/getRouters")
    public Result<List<RouterVo>> getRouters() {
        // 获取userId，这里先固定一个，后续通过thread local配合拦截器添加
        List<SysMenu> menus = menuService.selectMenuList(1L);
        return Result.success("获取成功", menuService.buildMenus(menus));

    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public Result getInfo()
    {
//        LoginUser loginUser = SecurityUtils.getLoginUser();
//        SysUser user = loginUser.getUser();
        // 角色集合
        SysUser user = userService.getUserByUserId(1L);
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
//        if (!loginUser.getPermissions().equals(permissions))
//        {
//            loginUser.setPermissions(permissions);
//            tokenService.refreshToken(loginUser);
//        }
        HashMap<String, Object> res = new HashMap<>();
        res.put("roles", roles);
        res.put("permissions", permissions);
//        AjaxResult ajax = AjaxResult.success();
//        ajax.put("user", user);
//        ajax.put("roles", roles);
//        ajax.put("permissions", permissions);
//        ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
//        ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
        return Result.success("成功",res);
    }
}
