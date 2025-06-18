package com.chasion.rybackend.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chasion.rybackend.entities.SysRole;

import java.util.List;


public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRole> selectRolePermissionByUserId(Long userId);


}
