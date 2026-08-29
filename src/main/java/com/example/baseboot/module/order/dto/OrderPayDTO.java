package com.example.baseboot.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayDTO {

    /**
     * 支付方式: 1-支付宝, 2-微信支付, 3-银联, 4-余额支付
     */
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    /**
     * 外部交易流水号 (可选，模拟支付时自动生成)
     */
    private String tradeNo;
}
