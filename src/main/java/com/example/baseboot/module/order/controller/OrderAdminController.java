package com.example.baseboot.module.order.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.order.dto.*;
import com.example.baseboot.module.order.service.OrderService;
import com.example.baseboot.module.order.vo.OrderDetailVO;
import com.example.baseboot.module.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端订单履约与运营控制器
 */
@Tag(name = "05. 履约端订单管理 (OrderAdminController)", description = "商家与发货人员订单分页、详情查看、录入物流发货、修改收件人与关单处理")
@RestController
@RequestMapping("/api/order/admin")
@RequiredArgsConstructor
@LoginRequired
public class OrderAdminController {

    private final OrderService orderService;

    /**
     * 多条件分页检索全量订单
     */
    @Operation(summary = "履约端分页检索订单", description = "按订单编号、收件人、手机号、状态等多条件分页检索商户全量订单")
    @GetMapping("/page")
    public CommonResult<CommonPage<OrderVO>> pageAdminOrders(OrderQueryDTO queryDTO) {
        CommonPage<OrderVO> page = orderService.pageAdminOrders(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 查询订单全流程履约详情
     */
    @Operation(summary = "查询订单全流程履约详情", description = "履约端查看订单明细、收件地址、物流配送与支付信息")
    @GetMapping("/{id}")
    public CommonResult<OrderDetailVO> getAdminOrderDetail(@PathVariable("id") Long id) {
        OrderDetailVO detailVO = orderService.getOrderDetail(null, id, true);
        return CommonResult.success(detailVO);
    }

    /**
     * 订单发货 (录入物流公司与单号，流转为已发货)
     */
    @Operation(summary = "订单发货录入物流", description = "填写承运物流公司与快递单号，将待发货订单推进至已发货")
    @PostMapping("/{id}/delivery")
    public CommonResult<OrderDetailVO> deliveryOrder(@PathVariable("id") Long id,
                                                     @Valid @RequestBody OrderDeliveryDTO deliveryDTO) {
        OrderDetailVO detailVO = orderService.deliveryOrder(id, deliveryDTO);
        return CommonResult.success(detailVO, "发货成功");
    }

    /**
     * 修改收货人信息 (待发货前)
     */
    @Operation(summary = "修改收货人信息", description = "发货前客服协助买家修改收货人姓名、电话与省市区地址")
    @PutMapping("/{id}/receiver")
    public CommonResult<OrderDetailVO> updateReceiverInfo(@PathVariable("id") Long id,
                                                          @Valid @RequestBody OrderReceiverUpdateDTO updateDTO) {
        OrderDetailVO detailVO = orderService.updateReceiverInfo(id, updateDTO);
        return CommonResult.success(detailVO, "收货人信息修改成功");
    }

    /**
     * 后台强制取消订单 (释放库存)
     */
    @Operation(summary = "后台强制取消订单", description = "管理端强制取消订单并自动退回已扣减的库存")
    @PostMapping("/{id}/cancel")
    public CommonResult<OrderDetailVO> adminCancelOrder(@PathVariable("id") Long id,
                                                       @RequestBody(required = false) OrderCancelDTO cancelDTO) {
        OrderDetailVO detailVO = orderService.adminCancelOrder(id, cancelDTO);
        return CommonResult.success(detailVO, "后台取消订单成功");
    }

    /**
     * 后台关闭/售后退款订单 (状态置为已关闭，退回库存)
     */
    @Operation(summary = "后台关闭/售后退款订单", description = "将订单置为已关闭状态并释放商品库存，记录关闭原因")
    @PostMapping("/{id}/close")
    public CommonResult<OrderDetailVO> closeOrder(@PathVariable("id") Long id,
                                                  @RequestParam(value = "reason", required = false) String reason) {
        OrderDetailVO detailVO = orderService.closeOrder(id, reason);
        return CommonResult.success(detailVO, "订单已关闭并释放库存");
    }
}
