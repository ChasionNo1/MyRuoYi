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
import java.util.List;

@Service
public class SysMonitorServiceImpl extends ServiceImpl<SystemMonitorMapper, SystemMonitor> implements ISysMonitorService {

    private static final SystemInfo systemInfo = new SystemInfo();
    private static final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private static final OperatingSystem os = systemInfo.getOperatingSystem();
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public SystemMonitor getSysMonitorInfo() {
        SystemMonitor monitor = new SystemMonitor();

        // 获取并设置CPU信息
        setCpuInfo(monitor);

        // 获取并设置内存信息
        setMemoryInfo(monitor);

        // 获取并设置磁盘信息
        setDiskInfo(monitor);

        // 获取并设置JVM信息
        setJvmInfo(monitor);

        return monitor;
    }

    private void setCpuInfo(SystemMonitor monitor) {
        CentralProcessor processor = hardware.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

        monitor.setCpuUsage(df.format(cpuUsage) + "%")
                .setCpuCoreNum(processor.getLogicalProcessorCount())
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

        for (oshi.software.os.OSFileStore fs : fileStores) {
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
        // 获取JVM内存信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

        // 获取线程信息
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        // 获取JVM版本信息
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

        monitor.setHeapUsed(formatBytes(heapMemory.getUsed()))
                .setHeapMax(formatBytes(heapMemory.getMax()))
                .setNonHeapUsed(formatBytes(nonHeapMemory.getUsed()))
                .setNonHeapMax(formatBytes(nonHeapMemory.getMax()))
                .setThreadActive(threadBean.getThreadCount())
                .setJvmVersion(runtimeBean.getVmName() + " " + runtimeBean.getVmVersion());
    }

    /**
     * 格式化字节大小为易读格式 (B, KB, MB, GB, TB)
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unitPrefix = "KMGTPE".charAt(exp - 1);
        return df.format(bytes / Math.pow(1024, exp)) + " " + unitPrefix + "B";
    }
}