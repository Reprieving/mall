package com.example.baseboot.module.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单结算预览视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费金额
     */
    private BigDecimal freightAmount;

    /**
     * 应付总金额 (商品总额 + 运费)
     */
    private BigDecimal payAmount;

    /**
     * 商品总件数
     */
    private Integer totalQuantity;

    /**
     * 结算商品明细预览列表
     */
    @Builder.Default
    private List<OrderItemVO> items = new ArrayList<>();
}
