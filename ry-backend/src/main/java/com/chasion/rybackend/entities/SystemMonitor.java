package com.chasion.rybackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 系统信息
 * @date 2025/6/19 14:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SystemMonitor extends BaseEntity{
    /** cpu 使用率 */
    private String cpuUsage;

    /** cpu 核心数 */
    private Integer cpuCoreNum;

    /** cpu 名称 */
    private String cpuName;

    /** cpu 频率 */
    private String cpuFrequency;

    /** 内存使用率 */
    private String memoryUsage;

    /** 内存总量 */
    private String memoryTotal;

    /** 内存剩余 */
    private String memoryFree;

    /** 内存使用 */
    private String memoryUsed;

    /** 磁盘总量 */
    private String diskTotal;

    /** 磁盘剩余 */
    private String diskFree;

    /** 磁盘使用 */
    private String diskUsed;

    /** 磁盘使用率 */
    private String diskUsage;

    /** 堆内存使用量 */
    private String heapUsed;

    /** 堆内存最大值 */
    private String heapMax;

    /** 非堆内存使用量 */
    private String nonHeapUsed;

    /** 非堆内存最大值 */
    private String nonHeapMax;

    /** 活动线程数 */
    private Integer threadActive;

    /** jvm 版本 */
    private String jvmVersion;

}
