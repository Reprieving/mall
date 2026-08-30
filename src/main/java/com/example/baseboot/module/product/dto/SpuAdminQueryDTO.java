package com.example.baseboot.module.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营端商品跨店铺多条件高级检索参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuAdminQueryDTO {

    /**
     * 商品名称 / 卖点关键字
     */
    private String keyword;

    /**
     * 商品编码
     */
    private String spuCode;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属品牌ID
     */
    private Long brandId;

    /**
     * 上架状态: 0-下架, 1-上架
     */
    private Integer status;

    /**
     * 是否仅查询库存告急商品
     */
    private Boolean lowStock;

    /**
     * 库存告急阈值 (默认 <= 10)
     */
    @Builder.Default
    private Integer lowStockThreshold = 10;

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    @Builder.Default
    private Long pageSize = 10L;
}
