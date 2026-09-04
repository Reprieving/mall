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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端订单控制器
 */
@Tag(name = "04. 买家端订单业务 (OrderController)", description = "买家结算预估、提交下单、支付、取消、确认收货与订单分页详情")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@LoginRequired
public class OrderController {

    private final OrderService orderService;

    /**
     * 订单结算预览 (核算金额、运费与商品可售性)
     */
    @Operation(summary = "结算页订单费用预估", description = "计算商品总价、运费、优惠券抵扣及最终应付金额，并校验各单品库存")
    @PostMapping("/preview")
    public CommonResult<OrderPreviewVO> previewOrder(@Valid @RequestBody OrderPreviewDTO previewDTO) {
        Long userId = UserContext.getUserId();
        OrderPreviewVO previewVO = orderService.previewOrder(userId, previewDTO);
        return CommonResult.success(previewVO);
    }

    /**
     * 创建并提交订单 (扣减/锁定库存、生成订单与商品快照)
     */
    @Operation(summary = "提交下单并锁定库存", description = "创建主订单与商品条目快照，流转为待付款状态")
    @PostMapping("/create")
    public CommonResult<OrderDetailVO> createOrder(@Valid @RequestBody OrderCreateDTO createDTO) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.createOrder(userId, createDTO);
        return CommonResult.success(detailVO, "订单提交成功");
    }

    /**
     * 订单支付 (模拟/发起支付)
     */
    @Operation(summary = "订单支付", description = "完成订单支付并记录支付时间与流水，状态变为待发货")
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
    @Operation(summary = "买家取消订单", description = "买家主动取消未支付订单并释放预占库存")
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
    @Operation(summary = "买家确认收货", description = "买家确认收货并将订单流转为已完成状态")
    @PostMapping("/{id}/receive")
    public CommonResult<OrderDetailVO> receiveOrder(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.receiveOrder(userId, id);
        return CommonResult.success(detailVO, "确认收货成功");
    }

    /**
     * 用户逻辑删除订单 (仅允许删除已完成/已取消/已关闭的订单)
     */
    @Operation(summary = "买家删除订单", description = "逻辑删除已完成或已关闭的历史订单记录")
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
    @Operation(summary = "查询买家订单详情", description = "获取指定订单的详细条目、收货地址与状态")
    @GetMapping("/{id}")
    public CommonResult<OrderDetailVO> getOrderDetail(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        OrderDetailVO detailVO = orderService.getOrderDetail(userId, id, false);
        return CommonResult.success(detailVO);
    }

    /**
     * 分页查询我的订单列表
     */
    @Operation(summary = "分页查询买家订单列表", description = "按订单状态与关键字多条件分页检索当前登录用户的订单")
    @GetMapping("/my-page")
    public CommonResult<CommonPage<OrderVO>> pageMyOrders(OrderQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        CommonPage<OrderVO> page = orderService.pageUserOrders(userId, queryDTO);
        return CommonResult.success(page);
    }
}
