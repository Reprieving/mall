package com.example.baseboot.module.product.spu.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运营端商品库存告急预警视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockWarningVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spuId;
    private String spuName;
    private String spuCode;
    private String spuPic;
    private Long shopId;
    private String shopName;
    private Long categoryId;
    private String categoryName;
    private Integer totalStock;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer status;
}
