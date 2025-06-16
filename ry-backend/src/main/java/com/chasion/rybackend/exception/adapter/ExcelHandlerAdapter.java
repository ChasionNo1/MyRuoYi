package com.chasion.rybackend.exception.adapter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author chasion
 * @version 1.0
 * @description: TODO Excel数据格式处理适配器
 * @date 2025/6/16 19:41
 */
public interface ExcelHandlerAdapter {
    /**
     * 格式化
     *
     * @param value 单元格数据值
     * @param args excel注解args参数组
     * @param cell 单元格对象
     * @param wb 工作簿对象
     *
     * @return 处理后的值
     */
    Object format(Object value, String[] args, Cell cell, Workbook wb);
}