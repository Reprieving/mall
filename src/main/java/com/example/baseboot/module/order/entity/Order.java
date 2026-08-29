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
 * 订单主表实体类 (对应表 oms_order)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oms_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 下单用户ID
     */
    private Long userId;

    /**
     * 订单流水号 (全局唯一)
     */
    private String orderSn;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费金额
     */
    private BigDecimal freightAmount;

    /**
     * 应付/实付总金额
     */
    private BigDecimal payAmount;

    /**
     * 支付方式: 0-未支付, 1-支付宝, 2-微信支付, 3-银联, 4-余额支付
     */
    private Integer payType;

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
     * 省份/直辖市
     */
    private String receiverProvince;

    /**
     * 城市
     */
    private String receiverCity;

    /**
     * 区/县
     */
    private String receiverDistrict;

    /**
     * 详细收货地址
     */
    private String receiverDetailAddress;

    /**
     * 买家留言/订单备注
     */
    private String note;

    /**
     * 物流配送公司 (如: 顺丰速运, 中通快递)
     */
    private String deliveryCompany;

    /**
     * 物流快递单号
     */
    private String deliverySn;

    /**
     * 第三方支付交易流水号
     */
    private String tradeNo;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 确认收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因说明
     */
    private String cancelReason;

    /**
     * 运营内部备注
     */
    private String adminRemark;

    /**
     * 运营插旗标色: 0-无, 1-红, 2-黄, 3-绿, 4-蓝, 5-紫
     */
    private Integer adminFlag;

    /**
     * 用户逻辑删除状态: 0-未删除, 1-已删除
     */
    private Integer deleteStatus;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
