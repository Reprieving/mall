package com.example.baseboot.module.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.order.dto.*;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.vo.OrderDetailVO;
import com.example.baseboot.module.order.vo.OrderPreviewVO;
import com.example.baseboot.module.order.vo.OrderVO;

/**
 * 订单核心业务服务接口
 */
public interface OrderService extends IService<Order> {

    /**
     * 订单结算预览 (核算金额、运费与商品可售性)
     */
    OrderPreviewVO previewOrder(Long userId, OrderPreviewDTO previewDTO);

    /**
     * 创建并提交订单 (高并发防超卖分布式锁、扣减库存、落库快照)
     */
    OrderDetailVO createOrder(Long userId, OrderCreateDTO createDTO);

    /**
     * 订单支付 (模拟/发起支付，状态流转为待发货)
     */
    OrderDetailVO payOrder(Long userId, Long orderId, OrderPayDTO payDTO);

    /**
     * 用户主动取消订单 (未支付订单取消并自动释放库存)
     */
    OrderDetailVO cancelOrder(Long userId, Long orderId, OrderCancelDTO cancelDTO);

    /**
     * 用户确认收货 (已发货 -> 已完成)
     */
    OrderDetailVO receiveOrder(Long userId, Long orderId);

    /**
     * 用户逻辑删除订单 (仅允许删除已完成/已取消的订单)
     */
    boolean deleteOrder(Long userId, Long orderId);

    /**
     * 查询订单详情 (用户端/管理端)
     */
    OrderDetailVO getOrderDetail(Long userId, Long orderId, boolean isAdmin);

    /**
     * 用户端分页查询我的订单列表
     */
    CommonPage<OrderVO> pageUserOrders(Long userId, OrderQueryDTO queryDTO);

    /**
     * 管理端多条件分页查询订单列表
     */
    CommonPage<OrderVO> pageAdminOrders(OrderQueryDTO queryDTO);

    /**
     * 订单发货 (管理端：填写物流公司与单号，状态流转为已发货)
     */
    OrderDetailVO deliveryOrder(Long orderId, OrderDeliveryDTO deliveryDTO);

    /**
     * 修改收货人信息 (管理端/待发货前)
     */
    OrderDetailVO updateReceiverInfo(Long orderId, OrderReceiverUpdateDTO updateDTO);

    /**
     * 管理端强制取消订单 (释放库存)
     */
    OrderDetailVO adminCancelOrder(Long orderId, OrderCancelDTO cancelDTO);

    /**
     * 管理端关闭/售后退款订单 (状态置为已关闭，退回库存)
     */
    OrderDetailVO closeOrder(Long orderId, String closeReason);
}
