package com.example.baseboot.module.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单发货请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryDTO {

    /**
     * 物流配送公司 (如: 顺丰速运, 中通快递)
     */
    @NotBlank(message = "物流公司名称不能为空")
    private String deliveryCompany;

    /**
     * 物流快递单号
     */
    @NotBlank(message = "快递单号不能为空")
    private String deliverySn;
}
