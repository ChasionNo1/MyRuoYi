package com.chasion.rybackend.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chasion.rybackend.entities.SysRole;
import com.chasion.rybackend.entities.SysUserRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/14 22:41
 */

public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 通过用户账号查询角色编码集合
     * @param username 用户账号名称
     * @return List<String>
     */
    @Select("select role_code from sys_role where role_id in (select role_id from sys_user_role where user_id = (select user_id from sys_user where user_name=#{username}))")
    List<String> getRoleCodeByUserName(@Param("username") String username);

    /**
     * 通过用户账号查询
     * @param username 用户账号名称
     * @return List<SysRole>
     */
    @Select("select * from sys_role where role_id in (select role_id from sys_user_role where user_id = (select user_id from sys_user where user_name=#{username}))")
    List<SysRole> getRolesByUsername(String username);
}