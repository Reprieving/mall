package com.example.baseboot.module.product.sku.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改 SKU 库存参数
 */
@Data
public class SkuStockUpdateDTO {

    /**
     * 当前实际物理库存
     */
    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    private Integer stock;
}
