package com.chasion.rybackend.service;

import com.chasion.rybackend.entities.dto.LogDTO;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 公共接口
 * @date 2025/6/15 16:09
 */
public interface BaseCommonService {

    void addLog(LogDTO logDTO);

    /**
     * 保存日志
     * @param logContent
     * @param logType
     * @param operateType
     */
    void addLog(String logContent, Integer logType, Integer operateType);
}