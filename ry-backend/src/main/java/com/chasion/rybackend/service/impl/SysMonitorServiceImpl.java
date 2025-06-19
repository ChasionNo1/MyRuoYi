package com.chasion.rybackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chasion.rybackend.entities.SystemMonitor;
import com.chasion.rybackend.mappers.SystemMonitorMapper;
import com.chasion.rybackend.service.ISysMonitorService;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.*;

@Service
public class SysMonitorServiceImpl extends ServiceImpl<SystemMonitorMapper, SystemMonitor> implements ISysMonitorService {

    private static final SystemInfo systemInfo = new SystemInfo();
    private static final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private static final OperatingSystem os = systemInfo.getOperatingSystem();
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public List<HashMap<String, Object>> getSysMonitorInfo() {
        SystemMonitor monitor = new SystemMonitor();

        // 获取系统信息
        setCpuInfo(monitor);
        setMemoryInfo(monitor);
        setDiskInfo(monitor);
        setJvmInfo(monitor);

        // 构建返回结果列表
        List<HashMap<String, Object>> resultList = new ArrayList<>();

        // 1. CPU信息组
        HashMap<String, Object> cpuGroup = new HashMap<>();
        cpuGroup.put("title", "cpu");
        HashMap<String, String> cpuData = new HashMap<>();
        cpuData.put("cpuUsage", monitor.getCpuUsage());
        cpuData.put("cpuCoreNum", monitor.getCpuCoreNum());
        cpuData.put("cpuName", monitor.getCpuName());
        cpuData.put("cpuFrequency", monitor.getCpuFrequency());
        cpuGroup.put("data", cpuData);
        resultList.add(cpuGroup);

        // 2. 内存信息组
        HashMap<String, Object> memoryGroup = new HashMap<>();
        memoryGroup.put("title", "memory");
        HashMap<String, String> memoryData = new HashMap<>();
        memoryData.put("memoryUsage", monitor.getMemoryUsage());
        memoryData.put("memoryTotal", monitor.getMemoryTotal());
        memoryData.put("memoryFree", monitor.getMemoryFree());
        memoryData.put("memoryUsed", monitor.getMemoryUsed());
        memoryGroup.put("data", memoryData);
        resultList.add(memoryGroup);

        // 3. 磁盘信息组
        HashMap<String, Object> diskGroup = new HashMap<>();
        diskGroup.put("title", "disk");
        HashMap<String, String> diskData = new HashMap<>();
        diskData.put("diskUsage", monitor.getDiskUsage());
        diskData.put("diskTotal", monitor.getDiskTotal());
        diskData.put("diskFree", monitor.getDiskFree());
        diskData.put("diskUsed", monitor.getDiskUsed());
        diskGroup.put("data", diskData);
        resultList.add(diskGroup);

        // 4. JVM信息组
        HashMap<String, Object> jvmGroup = new HashMap<>();
        jvmGroup.put("title", "jvm");
        HashMap<String, String> jvmData = new HashMap<>();
        jvmData.put("heapUsage", monitor.getHeapUsage());
        jvmData.put("heapUsed", monitor.getHeapUsed());
        jvmData.put("heapMax", monitor.getHeapMax());
        jvmData.put("nonHeapUsed", monitor.getNonHeapUsed());
//        jvmData.put("nonHeapMax", monitor.getNonHeapMax());
        jvmData.put("threadActive", monitor.getThreadActive());
        jvmData.put("jvmVersion", monitor.getJvmVersion());
        jvmGroup.put("data", jvmData);
        resultList.add(jvmGroup);

        return resultList;
    }

    // 以下私有方法保持不变，与原始实现一致
    private void setCpuInfo(SystemMonitor monitor) {
        CentralProcessor processor = hardware.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        monitor.setCpuUsage(df.format(cpuUsage) + "%")
                .setCpuCoreNum(String.valueOf(processor.getLogicalProcessorCount()))
                .setCpuName(processor.getProcessorIdentifier().getName())
                .setCpuFrequency(df.format(processor.getProcessorIdentifier().getVendorFreq() / 1_000_000_000.0) + " GHz");
    }

    private void setMemoryInfo(SystemMonitor monitor) {
        GlobalMemory memory = hardware.getMemory();
        long totalMemory = memory.getTotal();
        long availableMemory = memory.getAvailable();
        long usedMemory = totalMemory - availableMemory;
        double memoryUsage = (double) usedMemory / totalMemory * 100;

        monitor.setMemoryUsage(df.format(memoryUsage) + "%")
                .setMemoryTotal(formatBytes(totalMemory))
                .setMemoryFree(formatBytes(availableMemory))
                .setMemoryUsed(formatBytes(usedMemory));
    }

    private void setDiskInfo(SystemMonitor monitor) {
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        long totalSpace = 0;
        long freeSpace = 0;

        for (OSFileStore fs : fileStores) {
            totalSpace += fs.getTotalSpace();
            freeSpace += fs.getFreeSpace();
        }

        long usedSpace = totalSpace - freeSpace;
        double diskUsage = (double) usedSpace / totalSpace * 100;

        monitor.setDiskTotal(formatBytes(totalSpace))
                .setDiskFree(formatBytes(freeSpace))
                .setDiskUsed(formatBytes(usedSpace))
                .setDiskUsage(df.format(diskUsage) + "%");
    }

    private void setJvmInfo(SystemMonitor monitor) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        double jvmUsage = (double) heapMemory.getUsed() / heapMemory.getMax() * 100;

        monitor.setHeapUsage(df.format(jvmUsage) + "%")
                .setHeapUsed(formatBytes(heapMemory.getUsed()))
                .setHeapMax(formatBytes(heapMemory.getMax()))
                .setNonHeapUsed(formatBytes(nonHeapMemory.getUsed()))
                .setNonHeapMax(formatBytes(nonHeapMemory.getMax()))
                .setThreadActive(String.valueOf(threadBean.getThreadCount()))
                .setJvmVersion(runtimeBean.getVmName() + " " + runtimeBean.getVmVersion());
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unitPrefix = "KMGTPE".charAt(exp - 1);
        return df.format(bytes / Math.pow(1024, exp)) + " " + unitPrefix + "B";
    }
}