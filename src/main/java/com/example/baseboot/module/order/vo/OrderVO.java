package com.example.baseboot.module.order.vo;

import com.example.baseboot.module.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单简要列表视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String orderSn;
    private BigDecimal totalAmount;
    private BigDecimal freightAmount;
    private BigDecimal payAmount;
    private Integer payType;
    private Integer status;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverDetailAddress;
    private String note;
    private String deliveryCompany;
    private String deliverySn;
    private String tradeNo;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private String adminRemark;
    private Integer adminFlag;
    private LocalDateTime createTime;

    /**
     * 商品项数量统计
     */
    private Integer totalQuantity;

    /**
     * 封面首图
     */
    private String mainPic;

    public static OrderVO fromEntity(Order entity) {
        if (entity == null) {
            return null;
        }
        return OrderVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .orderSn(entity.getOrderSn())
                .totalAmount(entity.getTotalAmount())
                .freightAmount(entity.getFreightAmount())
                .payAmount(entity.getPayAmount())
                .payType(entity.getPayType())
                .status(entity.getStatus())
                .receiverName(entity.getReceiverName())
                .receiverPhone(entity.getReceiverPhone())
                .receiverProvince(entity.getReceiverProvince())
                .receiverCity(entity.getReceiverCity())
                .receiverDistrict(entity.getReceiverDistrict())
                .receiverDetailAddress(entity.getReceiverDetailAddress())
                .note(entity.getNote())
                .deliveryCompany(entity.getDeliveryCompany())
                .deliverySn(entity.getDeliverySn())
                .tradeNo(entity.getTradeNo())
                .paymentTime(entity.getPaymentTime())
                .deliveryTime(entity.getDeliveryTime())
                .receiveTime(entity.getReceiveTime())
                .cancelTime(entity.getCancelTime())
                .cancelReason(entity.getCancelReason())
                .adminRemark(entity.getAdminRemark())
                .adminFlag(entity.getAdminFlag() != null ? entity.getAdminFlag() : 0)
                .createTime(entity.getCreateTime())
                .build();
    }
}
