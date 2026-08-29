package com.example.baseboot.module.product.spec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改规格项请求参数
 */
@Data
public class SpecKeyUpdateDTO {

    /**
     * 所属商品分类ID (0为通用规格)
     */
    private Long categoryId;

    /**
     * 规格项名称
     */
    @NotBlank(message = "规格项名称不能为空")
    private String name;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
