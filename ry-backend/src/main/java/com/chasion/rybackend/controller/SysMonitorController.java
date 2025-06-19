package com.chasion.rybackend.controller;

import com.chasion.rybackend.entities.SystemMonitor;
import com.chasion.rybackend.resp.Result;
import com.chasion.rybackend.service.ISysMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO
 * @date 2025/6/19 15:19
 */
@RestController
@RequestMapping("/sys/monitor")
public class SysMonitorController {

    @Autowired
    private ISysMonitorService sysMonitorService;

    @RequestMapping("/getSystemInfo")
    public Result<List<HashMap<String, Object>>> getSysMonitorInfo() {
        List<HashMap<String, Object>> sysMonitorInfo = sysMonitorService.getSysMonitorInfo();
        return Result.success(sysMonitorInfo);
    }
}
