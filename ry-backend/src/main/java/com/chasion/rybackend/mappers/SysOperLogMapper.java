package com.chasion.rybackend.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chasion.rybackend.entities.SysOperLog;
import org.apache.ibatis.annotations.Update;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/16 22:50
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    @Update("truncate table sys_oper_log")
    public int cleanOperLog();
}