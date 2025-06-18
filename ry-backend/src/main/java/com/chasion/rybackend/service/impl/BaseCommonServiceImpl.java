package com.chasion.rybackend.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.chasion.rybackend.entities.LoginUser;
import com.chasion.rybackend.entities.SysRole;
import com.chasion.rybackend.entities.SysUser;
import com.chasion.rybackend.mappers.SysMenuMapper;
import com.chasion.rybackend.mappers.SysUserMapper;
import com.chasion.rybackend.mappers.SysUserRoleMapper;
import com.chasion.rybackend.service.IBaseCommonService;
import com.chasion.rybackend.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/15 23:36
 */
@Service
@Slf4j
public class BaseCommonServiceImpl implements IBaseCommonService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 通过数据库查询用户拥有的角色
     * @param username
     * @return
     */
    @Override
    public Set<String> queryUserRoles(String username) {
        List<String> roles = sysUserRoleMapper.getRoleCodeByUserName(username);
        log.info("-------通过数据库读取用户拥有的角色Rules------username： " + username + ",Roles size: " + (roles == null ? 0 : roles.size()));
        return new HashSet<>(roles);
    }

    /**
     * 通过数据库查询用户拥有的权限
     * ruoyi和jeecg两个在权限上命名方式不一样，ruoyi是menu，jeecg是permission
     * 参考的代码里，通过用户名去查权限，得到的permission对象，然后再遍历perm字段放到set里
     * ruoyi这里是通过id去查，在sql里去重，然后获取perm字段
     * 所以这里我感觉还是采用ruoyi的方式
     * @param username
     * @param loginType
     * @return
     */
    @Override
    public Set<String> queryUserAuths(String username, String loginType) {
        HashSet<String> permissionSet = new HashSet<>();
        List<String> perms = sysMenuMapper.selectMenuPermsByUsername(username);
        permissionSet.addAll(perms);
        log.info("-------通过数据库读取用户拥有的权限Rules------username： " + username + ",Roles size: " + (perms == null ? 0 : perms.size()));
        return permissionSet;
    }

    /**
     *
     * @param username
     * @return
     */
    @Override
    public LoginUser getUserByName(String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        LoginUser loginUser = new LoginUser();
        // 根据用户名从user表里查
        SysUser sysUser = sysUserMapper.selectUserByUsername(username);
        if (sysUser == null){
            return null;
        }
        BeanUtils.copyProperties(sysUser, loginUser);
        List<SysRole> roleList = sysUserRoleMapper.getRolesByUsername(username);
        if(!Objects.isNull(roleList) && CollectionUtil.isNotEmpty(roleList)){
            loginUser.setRoleIds(roleList.stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }


        return loginUser;
    }
}