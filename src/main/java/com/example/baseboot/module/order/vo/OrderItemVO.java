package com.example.baseboot.module.order.vo;

import com.example.baseboot.module.order.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品明细快照视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private String orderSn;
    private Long spuId;
    private String spuName;
    private String spuPic;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String skuPic;
    private BigDecimal skuPrice;
    private Integer quantity;
    private BigDecimal subtotalAmount;
    private String specData;
    private LocalDateTime createTime;

    public static OrderItemVO fromEntity(OrderItem entity) {
        if (entity == null) {
            return null;
        }
        return OrderItemVO.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .orderSn(entity.getOrderSn())
                .spuId(entity.getSpuId())
                .spuName(entity.getSpuName())
                .spuPic(entity.getSpuPic())
                .skuId(entity.getSkuId())
                .skuCode(entity.getSkuCode())
                .skuName(entity.getSkuName())
                .skuPic(entity.getSkuPic())
                .skuPrice(entity.getSkuPrice())
                .quantity(entity.getQuantity())
                .subtotalAmount(entity.getSubtotalAmount())
                .specData(entity.getSpecData())
                .createTime(entity.getCreateTime())
                .build();
    }
}
