package com.example.baseboot.module.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单退款申请分页查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundQueryDTO {

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页数量
     */
    private Long pageSize;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 关联订单流水号
     */
    private String orderSn;

    /**
     * 退款流水号
     */
    private String refundSn;

    /**
     * 审批状态: 0-待审核, 1-审核通过, 2-审核驳回
     */
    private Integer status;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 申请买家用户ID
     */
    private Long userId;
}
