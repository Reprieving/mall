package com.example.baseboot.module.admin.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运营控制台今日核心指标大屏概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 今日成交总额 (GMV)
     */
    private BigDecimal todayGmv;

    /**
     * 今日实付成交金额
     */
    private BigDecimal todayPayAmount;

    /**
     * 今日下单笔数
     */
    private Integer todayOrderCount;

    /**
     * 今日新增注册用户数
     */
    private Integer todayNewUsers;

    /**
     * 今日客单价
     */
    private BigDecimal todayAov;

    /**
     * 平台累计买家用户数
     */
    private Long totalUsers;

    /**
     * 平台累计开店商家数
     */
    private Long totalShops;

    /**
     * 平台在售商品数
     */
    private Long totalProducts;

    /**
     * 平台累计成交订单数
     */
    private Long totalOrders;

    /**
     * 平台累计成交总额 (GMV)
     */
    private BigDecimal totalGmv;
}
