package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.SysUser;

import java.util.Set;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO
 * @date 2025/6/19 09:43
 */
public interface ISysPermissionService {

    /**
     * 获取角色权限
     * @param user
     * @return
     */
    public Set<String> getRolePermission(SysUser user);

    /**
     * 获取菜单权限
     * @param user
     * @return
     */
    public Set<String> getMenuPermission(SysUser user);
}
