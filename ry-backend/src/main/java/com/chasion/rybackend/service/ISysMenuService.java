package com.chasion.rybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chasion.rybackend.entities.SysMenu;
import com.chasion.rybackend.entities.vo.RouterVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList(Long userId);

    public List<SysMenu> selectMenuList(SysMenu menu, Long userId);

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param
     * @return 菜单列表
     */
//    List<SysMenu> selectMenuTreeByUserId(Long userId);


    public List<SysMenu> buildMenuTree(List<SysMenu> menus);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    public Set<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectMenuPermsByUserId(Long userId);

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuTreeByUserId(Long userId);

}
