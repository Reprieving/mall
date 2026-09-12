package com.example.baseboot.module.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单完整详情视图对象 (包含主信息与全部商品项快照)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单主信息
     */
    private OrderVO orderInfo;

    /**
     * 订单商品项快照列表
     */
    @Builder.Default
    private List<OrderItemVO> items = new ArrayList<>();

    /**
     * 最新退款申请与审批记录 (若无则为 null)
     */
    private OrderRefundVO refundInfo;
}
