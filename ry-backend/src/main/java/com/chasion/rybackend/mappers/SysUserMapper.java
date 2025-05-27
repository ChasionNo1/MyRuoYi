package com.chasion.rybackend.mappers;

import com.chasion.rybackend.entities.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper {
    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    public int insertUser(SysUser user);

    @Select("select * from sys_user where user_name=#{username}")
    SysUser selectUserByUsername(String username);

    @Select("select * from sys_user where user_id=#{userId}")
    SysUser selectUserByUserId(Long id);
}
