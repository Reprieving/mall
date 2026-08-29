package com.example.baseboot.module.product.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新品牌请求参数
 */
@Data
public class BrandUpdateDTO {

    /**
     * 品牌名称
     */
    @NotBlank(message = "品牌名称不能为空")
    private String name;

    /**
     * 品牌Logo URL
     */
    private String logo;

    /**
     * 品牌描述
     */
    private String description;

    /**
     * 检索首字母 (大写 A-Z)
     */
    @Pattern(regexp = "^[A-Z]?$", message = "首字母必须为单个大写字母 A-Z")
    private String firstLetter;

    /**
     * 排序权重 (数字越小越靠前)
     */
    private Integer sort;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
