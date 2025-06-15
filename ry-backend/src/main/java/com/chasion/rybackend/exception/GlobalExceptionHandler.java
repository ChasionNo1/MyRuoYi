package com.chasion.rybackend.exception;

import com.chasion.rybackend.resp.Result;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO 全局异常处理
 * @date 2025/6/14 14:19
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private Result error;

    /**
     * @description: 处理异常业务
     * @param: e
     * @return:
 * @return com.chasion.rybackend.resp.Result<?>
     * @author chasion
     * @date: 2025/6/14 14:20
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    /** 
     * @description: 处理参数校验异常
     * @param: e
     * @return: 
 * @return com.chasion.rybackend.resp.Result<?>
     * @author chasion
     * @date: 2025/6/14 14:21
     */ 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败: {}", errorMsg);
        return Result.error(400, errorMsg);
    }
    /** 
     * @description: 当 Token 过期时会自动抛出该异常
     * @param: e
     * @return: 
 * @return com.chasion.rybackend.resp.Result
     * @author chasion
     * @date: 2025/6/14 14:21
     */ 
    @ExceptionHandler(ExpiredJwtException.class)
    public Result handleExpiredJwtException(ExpiredJwtException e) {
        return Result.error(401, "访问令牌已过期，请刷新");
    }
    
    /** 
     * @description: 处理所有未捕获异常
     * @param: e
     * @returns: com.chasion.rybackend.resp.Result<?>
     * @author chasion
     * @date: 2025/6/14 14:33
     */ 
    @ExceptionHandler(Exception.class)
    public Result<?> handleAllException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}