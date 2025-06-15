package com.chasion.rybackend.service.impl;

import com.chasion.rybackend.entities.dto.LogDTO;
import com.chasion.rybackend.service.BaseCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/15 23:36
 */
@Service
@Slf4j
public class BaseCommonServiceImpl implements BaseCommonService {
    @Override
    public void addLog(LogDTO logDTO) {

    }

    @Override
    public void addLog(String logContent, Integer logType, Integer operateType) {

    }
}