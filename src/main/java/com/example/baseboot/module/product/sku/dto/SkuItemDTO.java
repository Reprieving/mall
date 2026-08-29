package com.example.baseboot.module.product.sku.dto;

import com.example.baseboot.module.product.spec.dto.SkuSpecValueItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU 条目传输对象 (用于 SPU 录入或 SKU 批量维护)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuItemDTO {

    /**
     * SKU ID (编辑已有SKU时传入，新建则留空)
     */
    private Long id;

    /**
     * SKU 编码/条形码
     */
    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    /**
     * SKU 名称
     */
    @NotBlank(message = "SKU名称不能为空")
    private String name;

    /**
     * SKU 专属图片
     */
    private String pic;

    /**
     * 销售价格
     */
    @NotNull(message = "SKU售价不能为空")
    @DecimalMin(value = "0.00", message = "SKU售价不能小于0")
    private BigDecimal price;

    /**
     * 市场划线价
     */
    private BigDecimal originalPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "SKU库存不能小于0")
    private Integer stock;

    /**
     * 重量 (kg)
     */
    private BigDecimal weight;

    /**
     * 体积 (m³)
     */
    private BigDecimal volume;

    /**
     * 绑定的规格键值列表 (强关系绑定)
     */
    @Valid
    private List<SkuSpecValueItemDTO> specValues;

    /**
     * 多规格属性快照 (可选 JSON 字符串)
     */
    private String specData;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
