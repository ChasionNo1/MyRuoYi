package com.chasion.rybackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 字典类型
 * @date 2025/6/20 14:41
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class SysDictType extends BaseEntity{

    private static final long serialVersionUID = 1L;

    /**
     * 字典id
     */
    private Long dictId;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

}
