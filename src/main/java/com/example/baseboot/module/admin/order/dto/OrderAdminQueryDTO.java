package com.example.baseboot.module.admin.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 运营端全平台跨店铺订单多维高级检索参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAdminQueryDTO {

    /**
     * 订单流水号 (精确或模糊)
     */
    private String orderSn;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 物流单号
     */
    private String deliverySn;

    /**
     * 订单状态: 0-待付款, 1-待发货, 2-已发货, 3-已完成, 4-已取消, 5-已关闭
     */
    private Integer status;

    /**
     * 支付方式: 1-支付宝, 2-微信, 3-银联, 4-余额
     */
    private Integer payType;

    /**
     * 运营插旗标色: 1-红, 2-黄, 3-绿, 4-蓝, 5-紫
     */
    private Integer adminFlag;

    /**
     * 下单起始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 下单截止时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    @Builder.Default
    private Long pageSize = 10L;
}
