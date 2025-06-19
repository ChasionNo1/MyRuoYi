package com.chasion.rybackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chasion.rybackend.entities.SystemMonitor;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO
 * @date 2025/6/19 15:01
 */
public interface ISysMonitorService extends IService<SystemMonitor> {

    public SystemMonitor getSysMonitorInfo();
}
