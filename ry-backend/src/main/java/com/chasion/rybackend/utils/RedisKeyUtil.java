package com.chasion.rybackend.utils;

// 设置redis 的key
public class RedisKeyUtil {
    private static final String PREFIX_VERIFY_CODE = "verify:code";
    private static final String PREFIX_VERIFY_IMAGE = "verify:image";
    private static final String PREFIX_VERIFY_CODE_LOCK = "verify:code:lock";
    private static final String PREFIX_VERIFY_IMAGE_LOCK = "verify:image:lock";
    private static final String PREFIX_REFRESH_TOKEN = "refresh:token";
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    // 向某个邮箱发送验证码
    public static String getPrefixVerifyCode(String email) {
        return PREFIX_VERIFY_CODE + ":" + email;
    }

    public static String getPrefixVerifyCodeLock(String email) {
        return PREFIX_VERIFY_CODE_LOCK + ":" + email;
    }

    public static String getPrefixVerifyImageLock(String ip){
        return PREFIX_VERIFY_IMAGE_LOCK + ":" + ip;
    }

    public static String getPrefixRefreshTokenKey(Long id) {
        return PREFIX_REFRESH_TOKEN + ":" + id;
    }

    public static String getPrefixVerifyImageKey(String uuid) {
        return PREFIX_VERIFY_IMAGE + ":" + uuid;
    }
}