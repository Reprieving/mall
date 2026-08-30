package com.example.baseboot.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运营待办事项汇总角标
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTodosVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待审核主体实名认证数
     */
    private Long pendingCertCount;

    /**
     * 待审核开店申请数
     */
    private Long pendingShopCount;

    /**
     * 待发货订单数
     */
    private Long pendingDeliverCount;

    /**
     * 库存告急预警商品数 (总库存 <= 10)
     */
    private Long lowStockProductCount;
}
