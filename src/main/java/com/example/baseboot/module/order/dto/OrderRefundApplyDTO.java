package com.example.baseboot.module.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 买家申请订单退款参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundApplyDTO {

    /**
     * 退款类型: 1-仅退款, 2-退货退款 (默认 1)
     */
    private Integer refundType;

    /**
     * 申请退款金额 (可选，若不传则默认为订单实付全额)
     */
    @DecimalMin(value = "0.01", message = "申请退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    private String reason;

    /**
     * 退款详细说明 / 补充描述
     */
    private String description;

    /**
     * 凭证图片URL (多张逗号分隔或JSON)
     */
    private String proofPics;
}
