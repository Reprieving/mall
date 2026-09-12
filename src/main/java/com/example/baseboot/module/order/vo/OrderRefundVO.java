package com.example.baseboot.module.order.vo;

import com.example.baseboot.module.order.entity.OrderRefund;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款申请视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退款记录ID
     */
    private Long id;

    /**
     * 退款流水号
     */
    private String refundSn;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 关联订单流水号
     */
    private String orderSn;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 所属店铺名称
     */
    private String shopName;

    /**
     * 申请买家ID
     */
    private Long userId;

    /**
     * 退款类型: 1-仅退款, 2-退货退款
     */
    private Integer refundType;

    /**
     * 退款类型描述
     */
    private String refundTypeDesc;

    /**
     * 申请退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 详细说明
     */
    private String description;

    /**
     * 凭证图片URL
     */
    private String proofPics;

    /**
     * 审批状态: 0-待审核, 1-审核通过, 2-审核驳回
     */
    private Integer status;

    /**
     * 审批状态描述
     */
    private String statusDesc;

    /**
     * 审批时间
     */
    private LocalDateTime auditTime;

    /**
     * 审批人ID
     */
    private Long auditUserId;

    /**
     * 审批人姓名/账号
     */
    private String auditUserName;

    /**
     * 审批批注/驳回原因
     */
    private String auditRemark;

    /**
     * 申请创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public static OrderRefundVO fromEntity(OrderRefund entity) {
        if (entity == null) {
            return null;
        }

        String typeDesc = entity.getRefundType() != null && entity.getRefundType() == 2 ? "退货退款" : "仅退款";
        String statusDesc = switch (entity.getStatus() != null ? entity.getStatus() : 0) {
            case 1 -> "审核通过";
            case 2 -> "审核驳回";
            default -> "待审核";
        };

        return OrderRefundVO.builder()
                .id(entity.getId())
                .refundSn(entity.getRefundSn())
                .orderId(entity.getOrderId())
                .orderSn(entity.getOrderSn())
                .shopId(entity.getShopId())
                .userId(entity.getUserId())
                .refundType(entity.getRefundType())
                .refundTypeDesc(typeDesc)
                .refundAmount(entity.getRefundAmount())
                .reason(entity.getReason())
                .description(entity.getDescription())
                .proofPics(entity.getProofPics())
                .status(entity.getStatus())
                .statusDesc(statusDesc)
                .auditTime(entity.getAuditTime())
                .auditUserId(entity.getAuditUserId())
                .auditUserName(entity.getAuditUserName())
                .auditRemark(entity.getAuditRemark())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
