package com.example.baseboot.module.product.spec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 创建规格项请求参数
 */
@Data
public class SpecKeyCreateDTO {

    /**
     * 所属商品分类ID (0为通用规格)
     */
    private Long categoryId;

    /**
     * 规格项名称 (如: 机身颜色, 存储容量, 尺码)
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

    /**
     * 附带创建的初始规格值列表 (可选)
     */
    private List<String> initialValues;
}
