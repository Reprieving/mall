package com.example.baseboot.module.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消订单请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelDTO {

    /**
     * 取消原因
     */
    private String cancelReason;
}
