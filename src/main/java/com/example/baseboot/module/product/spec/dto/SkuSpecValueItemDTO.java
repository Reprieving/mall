package com.example.baseboot.module.product.spec.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SKU 绑定的单条规格键值对参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuSpecValueItemDTO {

    /**
     * 规格项ID
     */
    @NotNull(message = "规格项ID不能为空")
    private Long specKeyId;

    /**
     * 规格项名称 (可选，若不传系统自动根据 ID 填充)
     */
    private String specKeyName;

    /**
     * 规格值ID
     */
    @NotNull(message = "规格值ID不能为空")
    private Long specValueId;

    /**
     * 规格值内容 (可选，若不传系统自动根据 ID 填充)
     */
    private String specValue;
}
