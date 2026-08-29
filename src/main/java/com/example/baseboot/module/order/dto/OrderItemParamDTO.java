package com.example.baseboot.module.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单/预览商品条目参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemParamDTO {

    /**
     * 商品 SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为1件")
    private Integer quantity;
}
