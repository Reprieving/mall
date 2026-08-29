package com.example.baseboot.module.admin.order.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单生命周期流转轨迹日志视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件发生时间
     */
    private LocalDateTime time;

    /**
     * 动作名称 (如: 创建订单, 买家支付, 后台发货, 确认收货, 取消订单)
     */
    private String action;

    /**
     * 操作人角色 / 身份 (如: 买家, 平台运营, 系统自动)
     */
    private String operator;

    /**
     * 详细说明 (如: 支付方式, 物流单号, 取消原因)
     */
    private String detail;
}
