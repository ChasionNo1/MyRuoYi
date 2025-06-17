package com.chasion.rybackend.filter;

import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;

/**
 * @author 32260
 * @version 1.0
 * @description: TODO 排除json 敏感属性
 * @date 2025/6/17 11:22
 */
public class PropertyPreExcludeFilter extends SimplePropertyPreFilter {
    public PropertyPreExcludeFilter() {
    }

    public PropertyPreExcludeFilter addExcludes(String... filters) {
        for (int i = 0; i < filters.length; i++) {
            this.getExcludes().add(filters[i]);
        }
        return this;
    }
}
