package com.chasion.rybackend.aspectj.lang.enums;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO
 * @date 2025/6/14 15:13
 */
public enum LimitType {

    /**
     * 默认策略全局限流
     */
    DEFAULT,

    /**
     * 根据请求者IP进行限流
     */
    IP
}
