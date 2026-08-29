package com.example.baseboot.module.product.sku.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 修改 SKU 价格参数
 */
@Data
public class SkuPriceUpdateDTO {

    /**
     * 销售价格
     */
    @NotNull(message = "销售价格不能为空")
    @DecimalMin(value = "0.00", message = "销售价格不能小于0")
    private BigDecimal price;

    /**
     * 市场划线价
     */
    private BigDecimal originalPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;
}
