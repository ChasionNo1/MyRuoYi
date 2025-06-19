package com.chasion.rybackend.controller;

import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.service.ISysPermissionService;
import com.chasion.rybackend.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Set;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 用户相关的接口
 * @date 2025/6/19 09:38
 */
@RestController
@RequestMapping("/sys/user")
@Tag(name = "用户接口")
public class SysUserController {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysPermissionService permissionService;

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    @Operation(summary = "响应用户信息接口")
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
