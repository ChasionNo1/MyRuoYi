package com.chasion.rybackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 字典数据
 * @date 2025/6/20 14:41
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SysDictData extends BaseEntity{

    private static final long serialVersionUID = 1L;

    /** 字典数据主键 */
    private Long id;

    /** 字典排序 */
    private Long dictSort;

    /** 字典标签 */
    private String dictLabel;

    /** 字典键值 */
    private String dictValue;

    /** 字典类型 */
    private String dictType;

    /** 样式属性（其他样式扩展） */
    private String cssClass;

    /** 表格回显样式 */
    private String listClass;

    /** 是否默认（Y是 N否） */
    private String isDefault;

    /** 状态（0正常 1停用） */
    private String status;


}
