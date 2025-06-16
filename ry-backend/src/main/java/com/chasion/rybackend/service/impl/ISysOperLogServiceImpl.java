package com.chasion.rybackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chasion.rybackend.entities.SysOperLog;
import com.chasion.rybackend.mappers.SysOperLogMapper;
import com.chasion.rybackend.service.ISysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/16 22:50
 */
@Service
public class ISysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    @Autowired
    private SysOperLogMapper operLogMapper;

    /**
     * 新增操作日志
     *
     * @param operLog 操作日志对象
     */
    @Override
    public void insertOperlog(SysOperLog operLog) {
        operLogMapper.insert(operLog);
    }

    /**
     * 查询系统操作日志集合
     *
     * @param operLog 操作日志对象
     * @return 操作日志集合
     */
    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog) {
        QueryWrapper<SysOperLog> queryWrapper = new QueryWrapper<>();

        // 处理基本查询条件
        queryWrapper
                .like(operLog.getOperIp() != null && !operLog.getOperIp().isEmpty(), "oper_ip", operLog.getOperIp())
                .like(operLog.getTitle() != null && !operLog.getTitle().isEmpty(), "title", operLog.getTitle())
                .eq(operLog.getBusinessType() != null, "business_type", operLog.getBusinessType())
                .eq(operLog.getStatus() != null, "status", operLog.getStatus())
                .like(operLog.getOperName() != null && !operLog.getOperName().isEmpty(), "oper_name", operLog.getOperName());

        // 处理in条件
        if (operLog.getBusinessTypes() != null && operLog.getBusinessTypes().length > 0) {
            queryWrapper.in("business_type", operLog.getBusinessTypes());
        }

        // 处理时间范围查询
        if (operLog.getParams() != null) {
            Object beginTime = operLog.getParams().get("beginTime");
            Object endTime = operLog.getParams().get("endTime");

            if (beginTime != null && !"".equals(beginTime)) {
                queryWrapper.ge("oper_time", beginTime);
            }

            if (endTime != null && !"".equals(endTime)) {
                queryWrapper.le("oper_time", endTime);
            }
        }

        // 添加排序
        queryWrapper.orderByDesc("oper_id");

        return operLogMapper.selectList(queryWrapper);
    }

    /**
     * 批量删除系统操作日志
     *
     * @param operIds 需要删除的操作日志ID
     * @return 结果
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        return operLogMapper.deleteByIds(Arrays.asList(operIds));
    }

    /**
     * 查询操作日志详细
     *
     * @param operId 操作ID
     * @return 操作日志对象
     */
    @Override
    public SysOperLog selectOperLogById(Long operId) {
        return operLogMapper.selectById(operId);
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog() {
        operLogMapper.cleanOperLog();
    }
}