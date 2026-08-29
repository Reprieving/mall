package com.example.baseboot.module.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单结算预览请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewDTO {

    /**
     * 目标收货省份 (用于运费核算)
     */
    private String receiverProvince;

    /**
     * 购买的商品 SKU 列表
     */
    @NotEmpty(message = "购买商品条目不能为空")
    @Valid
    private List<OrderItemParamDTO> items;
}
