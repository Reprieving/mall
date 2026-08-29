package com.example.baseboot.module.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单分页查询参数 (支持用户端与管理端)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderQueryDTO {

    /**
     * 用户ID (管理端可查指定用户，用户端自动取上下文)
     */
    private Long userId;

    /**
     * 订单流水号
     */
    private String orderSn;

    /**
     * 订单状态: 0-待付款, 1-待发货, 2-已发货, 3-已完成, 4-已取消, 5-已关闭
     */
    private Integer status;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 下单起始时间
     */
    private LocalDateTime startTime;

    /**
     * 下单截止时间
     */
    private LocalDateTime endTime;

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
