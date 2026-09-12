package com.example.baseboot.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款申请实体类 (对应表 oms_order_refund)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oms_order_refund")
public class OrderRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款流水号 (全局唯一)
     */
    private String refundSn;

    /**
     * 所属订单ID
     */
    private Long orderId;

    /**
     * 订单流水号
     */
    private String orderSn;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 申请买家用户ID
     */
    private Long userId;

    /**
     * 退款类型: 1-仅退款, 2-退货退款
     */
    private Integer refundType;

    /**
     * 申请退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String reason;

    /**
     * 退款详细说明
     */
    private String description;

    /**
     * 凭证图片URL (逗号分隔或JSON)
     */
    private String proofPics;

    /**
     * 审批状态: 0-待审核, 1-审核通过(已退款), 2-审核驳回(已拒绝)
     */
    private Integer status;

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
}
