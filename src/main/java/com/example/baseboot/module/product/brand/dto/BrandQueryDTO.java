package com.example.baseboot.module.product.brand.dto;

import lombok.Data;

/**
 * 品牌分页查询条件参数
 */
@Data
public class BrandQueryDTO {

    /**
     * 品牌名称关键字
     */
    private String name;

    /**
     * 检索首字母 (大写 A-Z)
     */
    private String firstLetter;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 当前页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
