package com.chasion.rybackend;

// springboot测试类

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class RybackendApplicationTests {

    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private final OperatingSystem os = systemInfo.getOperatingSystem();
    private final CentralProcessor processor = hardware.getProcessor();
    private final GlobalMemory memory = hardware.getMemory();
    private final DecimalFormat df = new DecimalFormat("0.00");

    // 测试方法
    void contextLoads() {
    }

    @Test
    public void testGetSystemInfo() {
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        Map<String, Object> cpuInfo = new HashMap<>();
        cpuInfo.put("usage", df.format(cpuUsage));
        cpuInfo.put("cores", processor.getLogicalProcessorCount());
        cpuInfo.put("name", processor.getProcessorIdentifier().getName());
        cpuInfo.put("frequency", df.format(processor.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0) + " GHz");
        System.out.println(cpuInfo);
    }

}