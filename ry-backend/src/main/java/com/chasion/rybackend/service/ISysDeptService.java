package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.SysDept;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * 部门表 服务实现类
 */
public interface ISysDeptService extends MPJBaseService<SysDept> {

    /**
     * 根据userId查用户所有部门
     * @param userId
     * @return
     */
    public List<SysDept> queryUserDeparts(Long userId);
}
