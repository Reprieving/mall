package com.example.baseboot.module.order.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.order.dto.*;
import com.example.baseboot.module.order.service.OrderService;
import com.example.baseboot.module.order.vo.OrderDetailVO;
import com.example.baseboot.module.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端订单履约与运营控制器
 */
@RestController
@RequestMapping("/api/order/admin")
@RequiredArgsConstructor
@LoginRequired
public class OrderAdminController {

    private final OrderService orderService;

    /**
     * 多条件分页检索全量订单
     */
    @GetMapping("/page")
    public CommonResult<CommonPage<OrderVO>> pageAdminOrders(OrderQueryDTO queryDTO) {
        CommonPage<OrderVO> page = orderService.pageAdminOrders(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 查询订单全流程履约详情
     */
    @GetMapping("/{id}")
    public CommonResult<OrderDetailVO> getAdminOrderDetail(@PathVariable("id") Long id) {
        OrderDetailVO detailVO = orderService.getOrderDetail(null, id, true);
        return CommonResult.success(detailVO);
    }

    /**
     * 订单发货 (录入物流公司与单号，流转为已发货)
     */
    @PostMapping("/{id}/delivery")
    public CommonResult<OrderDetailVO> deliveryOrder(@PathVariable("id") Long id,
                                                     @Valid @RequestBody OrderDeliveryDTO deliveryDTO) {
        OrderDetailVO detailVO = orderService.deliveryOrder(id, deliveryDTO);
        return CommonResult.success(detailVO, "发货成功");
    }

    /**
     * 修改收货人信息 (待发货前)
     */
    @PutMapping("/{id}/receiver")
    public CommonResult<OrderDetailVO> updateReceiverInfo(@PathVariable("id") Long id,
                                                          @Valid @RequestBody OrderReceiverUpdateDTO updateDTO) {
        OrderDetailVO detailVO = orderService.updateReceiverInfo(id, updateDTO);
        return CommonResult.success(detailVO, "收货人信息修改成功");
    }

    /**
     * 后台强制取消订单 (释放库存)
     */
    @PostMapping("/{id}/cancel")
    public CommonResult<OrderDetailVO> adminCancelOrder(@PathVariable("id") Long id,
                                                       @RequestBody(required = false) OrderCancelDTO cancelDTO) {
        OrderDetailVO detailVO = orderService.adminCancelOrder(id, cancelDTO);
        return CommonResult.success(detailVO, "后台取消订单成功");
    }

    /**
     * 后台关闭/售后退款订单 (状态置为已关闭，退回库存)
     */
    @PostMapping("/{id}/close")
    public CommonResult<OrderDetailVO> closeOrder(@PathVariable("id") Long id,
                                                  @RequestParam(value = "reason", required = false) String reason) {
        OrderDetailVO detailVO = orderService.closeOrder(id, reason);
        return CommonResult.success(detailVO, "订单已关闭并释放库存");
    }
}
