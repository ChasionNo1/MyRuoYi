package com.chasion.rybackend.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chasion.rybackend.entities.SysLogininfor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/16 22:30
 */

public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

    @Update("truncate table sys_logininfor")
    public int cleanLogininfor();
}