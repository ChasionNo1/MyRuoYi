package com.chasion.rybackend.exception;

import com.chasion.rybackend.resp.ResultCode;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 自定义异常类
 * @date 2025/6/14 14:08
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(final Integer code, final String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public Integer getCode() {
        return code;
    }
}