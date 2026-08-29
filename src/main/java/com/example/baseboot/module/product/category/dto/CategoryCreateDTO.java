package com.example.baseboot.module.product.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建商品分类请求参数
 */
@Data
public class CategoryCreateDTO {

    /**
     * 父分类ID (0为顶级分类)
     */
    @NotNull(message = "父分类ID不能为空")
    private Long parentId;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 分类层级: 1-一级, 2-二级, 3-三级
     */
    private Integer level;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 排序权重 (数字越小越靠前)
     */
    private Integer sort;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
