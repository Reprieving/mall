package com.example.baseboot.module.product.spec.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改规格值请求参数
 */
@Data
public class SpecValueUpdateDTO {

    /**
     * 规格值内容
     */
    @NotBlank(message = "规格值内容不能为空")
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
