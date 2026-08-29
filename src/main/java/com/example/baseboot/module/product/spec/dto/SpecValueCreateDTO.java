package com.example.baseboot.module.product.spec.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建规格值请求参数
 */
@Data
public class SpecValueCreateDTO {

    /**
     * 所属规格项ID
     */
    @NotNull(message = "规格项ID不能为空")
    private Long specKeyId;

    /**
     * 规格值 (如: 原色钛金属, 暗夜黑, 256GB)
     */
    @NotBlank(message = "规格值不能为空")
    private String value;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
