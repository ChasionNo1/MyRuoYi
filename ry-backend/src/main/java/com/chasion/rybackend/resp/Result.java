package com.chasion.rybackend.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 统一返回接口
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    //快速返回操作成功响应结果(带响应数据)
    public static <E> Result<E> success(String message, E data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    //快速返回操作成功响应结果
    public static Result success(String message) {
        return new Result(ResultCode.SUCCESS.getCode(), message, null);
    }

    public static Result error(Integer code, String message) {
        return new Result(code, message, null);
    }
}
