package com.chasion.rybackend.aspectj.lang.annotation;

import com.chasion.rybackend.aspectj.lang.enums.LimitType;
import com.chasion.rybackend.utils.RedisKeyUtil;
import io.lettuce.core.Limit;

import java.lang.annotation.*;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 限流注解
 * @date 2025/6/14 15:05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    // 限流key
    public String key() default RedisKeyUtil.RATE_LIMIT_KEY;

    // 限流时间，单位s
    public int time() default 60;

    // 限流次数
    public int count() default 1;

    // 限流类型 默认全局，可选ip
    public LimitType limitType() default LimitType.DEFAULT;
}