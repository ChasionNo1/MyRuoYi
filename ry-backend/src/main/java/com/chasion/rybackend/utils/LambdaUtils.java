package com.chasion.rybackend.utils;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO
 * @date 2025/6/18 11:34
 */
public class LambdaUtils {
    public static <T, R> String getColumeName(TypeFunction<T, R> name) {
        return TypeFunction.getLambdaColumnName(name);
    }
}
