package com.example.baseboot.module.product.spec.dto;

import lombok.Data;

/**
 * 规格项分页查询参数
 */
@Data
public class SpecKeyQueryDTO {

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 规格项名称关键字
     */
    private String name;

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
