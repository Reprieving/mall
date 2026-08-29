package com.example.baseboot.module.order.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.order.dto.*;
import com.example.baseboot.module.order.service.OrderService;
import com.example.baseboot.module.order.vo.OrderDetailVO;
import com.example.baseboot.module.order.vo.OrderPreviewVO;
import com.example.baseboot.module.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单控制器
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@LoginRequired
public class OrderController {

    private final OrderService orderService;

    /**
     * 订单结算预览 (核算金额、运费与商品可售性)
     */
    @PostMapping("/preview")
    public CommonResult<OrderPreviewVO> previewOrder(@Valid @RequestBody OrderPreviewDTO previewDTO) {
        Long userId = UserContext.getUserId();
        OrderPreviewVO previewVO = orderService.previewOrder(userId, previewDTO);
        return CommonResult.success(previewVO);
    }

    /**
     * 创建并提交订单 (扣减/锁定库存、生成订单与商品快照)
     */
    @PostMapping("/create")
    public CommonResult<OrderDetailVO> createOrder(@Valid @RequestBody OrderCreateDTO createDTO) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.createOrder(userId, createDTO);
        return CommonResult.success(detailVO, "订单提交成功");
    }

    /**
     * 订单支付 (模拟/发起支付)
     */
    @PostMapping("/{id}/pay")
    public CommonResult<OrderDetailVO> payOrder(@PathVariable("id") Long id,
                                                @Valid @RequestBody OrderPayDTO payDTO) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.payOrder(userId, id, payDTO);
        return CommonResult.success(detailVO, "订单支付成功");
    }

    /**
     * 用户主动取消订单 (未支付订单取消并释放库存)
     */
    @PostMapping("/{id}/cancel")
    public CommonResult<OrderDetailVO> cancelOrder(@PathVariable("id") Long id,
                                                  @RequestBody(required = false) OrderCancelDTO cancelDTO) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.cancelOrder(userId, id, cancelDTO);
        return CommonResult.success(detailVO, "订单已取消");
    }

    /**
     * 用户确认收货 (已发货 -> 已完成)
     */
    @PostMapping("/{id}/receive")
    public CommonResult<OrderDetailVO> receiveOrder(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.receiveOrder(userId, id);
        return CommonResult.success(detailVO, "确认收货成功");
    }

    /**
     * 用户逻辑删除订单 (仅允许删除已完成/已取消/已关闭的订单)
     */
    @DeleteMapping("/{id}")
    public CommonResult<Void> deleteOrder(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        boolean success = orderService.deleteOrder(userId, id);
        if (success) {
            return CommonResult.success(null, "订单删除成功");
        }
        return CommonResult.failed("订单删除失败");
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public CommonResult<OrderDetailVO> getOrderDetail(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.getOrderDetail(userId, id, false);
        return CommonResult.success(detailVO);
    }

    /**
     * 分页查询我的订单列表
     */
    @GetMapping("/my-page")
    public CommonResult<CommonPage<OrderVO>> pageMyOrders(OrderQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        CommonPage<OrderVO> page = orderService.pageUserOrders(userId, queryDTO);
        return CommonResult.success(page);
    }
}
