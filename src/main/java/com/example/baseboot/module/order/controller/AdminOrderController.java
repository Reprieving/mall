package com.example.baseboot.module.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.order.dto.OrderAdminQueryDTO;
import com.example.baseboot.module.order.dto.OrderAdminRemarkDTO;
import com.example.baseboot.module.order.vo.OrderAdminDetailVO;
import com.example.baseboot.module.order.vo.OrderLogVO;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.entity.OrderItem;
import com.example.baseboot.module.order.mapper.OrderItemMapper;
import com.example.baseboot.module.order.mapper.OrderMapper;
import com.example.baseboot.module.order.vo.OrderItemVO;
import com.example.baseboot.module.order.vo.OrderVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.entity.SysUser;
import com.example.baseboot.module.user.profile.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营端全平台跨店铺订单调度与流转跟踪控制器
 */
@Tag(name = "06. 运营端订单调度 (AdminOrderController)", description = "全平台跨店铺订单高级检索、全景流转画像、插旗标色与时间轴追踪")
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SysUserMapper sysUserMapper;
    private final SpuMapper spuMapper;
    private final ShopMapper shopMapper;

    /**
     * 全平台跨店铺订单多维组合高级检索
     */
    @Operation(summary = "全平台跨店铺订单高级检索", description = "支持按订单号、收件人、手机号、运单号、支付方式、插旗颜色等多维组合检索")
    @GetMapping("/page")
    @RequirePermission("order:view")
    public CommonResult<CommonPage<OrderVO>> pageOrders(OrderAdminQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new OrderAdminQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getOrderSn())) {
            wrapper.like(Order::getOrderSn, queryDTO.getOrderSn().trim());
        }
        if (StringUtils.hasText(queryDTO.getReceiverName())) {
            wrapper.like(Order::getReceiverName, queryDTO.getReceiverName().trim());
        }
        if (StringUtils.hasText(queryDTO.getReceiverPhone())) {
            wrapper.like(Order::getReceiverPhone, queryDTO.getReceiverPhone().trim());
        }
        if (StringUtils.hasText(queryDTO.getDeliverySn())) {
            wrapper.like(Order::getDeliverySn, queryDTO.getDeliverySn().trim());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getPayType() != null) {
            wrapper.eq(Order::getPayType, queryDTO.getPayType());
        }
        if (queryDTO.getAdminFlag() != null) {
            wrapper.eq(Order::getAdminFlag, queryDTO.getAdminFlag());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Order::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Order::getCreateTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(Order::getId);
        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        if (CollectionUtils.isEmpty(orderPage.getRecords())) {
            return CommonResult.success(CommonPage.restPage(orderPage, Collections.emptyList()));
        }

        List<Long> orderIds = orderPage.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        Map<Long, List<OrderItem>> orderItemMap = items.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<OrderVO> voList = orderPage.getRecords().stream().map(o -> {
            OrderVO vo = OrderVO.fromEntity(o);
            List<OrderItem> orderItems = orderItemMap.getOrDefault(o.getId(), Collections.emptyList());
            int totalQty = orderItems.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
            String mainPic = !orderItems.isEmpty() ? orderItems.get(0).getSkuPic() : null;
            if (!StringUtils.hasText(mainPic) && !orderItems.isEmpty()) {
                mainPic = orderItems.get(0).getSpuPic();
            }
            vo.setTotalQuantity(totalQty);
            vo.setMainPic(mainPic);
            return vo;
        }).collect(Collectors.toList());

        return CommonResult.success(CommonPage.restPage(orderPage, voList));
    }

    /**
     * 获取全量订单详情 (含买家、店铺、商品快照明细与流转时间轴)
     */
    @Operation(summary = "查询全量订单详情", description = "包含订单基本信息、收货人、买家画像、所属店铺、商品条目快照与流转时间线")
    @GetMapping("/{id}/detail")
    @RequirePermission("order:view")
    public CommonResult<OrderAdminDetailVO> getOrderDetail(@PathVariable("id") Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 商品快照
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id));
        List<OrderItemVO> itemVOs = items.stream().map(OrderItemVO::fromEntity).collect(Collectors.toList());

        // 买家资料
        SysUser buyer = sysUserMapper.selectById(order.getUserId());

        // 所属店铺 (通过首个条目的 spu 获取关联店铺)
        Shop shop = null;
        if (!items.isEmpty() && items.get(0).getSpuId() != null) {
            Spu spu = spuMapper.selectById(items.get(0).getSpuId());
            if (spu != null && spu.getShopId() != null) {
                shop = shopMapper.selectById(spu.getShopId());
            }
        }

        // 构建流转时间轴
        List<OrderLogVO> timeline = buildOrderTimeline(order);

        OrderAdminDetailVO detailVO = OrderAdminDetailVO.builder()
                .orderInfo(OrderVO.fromEntity(order))
                .items(itemVOs)
                .buyer(UserVO.fromEntity(buyer))
                .shop(ShopVO.fromEntity(shop))
                .timeline(timeline)
                .build();

        return CommonResult.success(detailVO);
    }

    /**
     * 运营订单插旗标色与添加内部备注
     */
    @Operation(summary = "运营订单插旗与添加备注", description = "设置订单插旗颜色（1-红, 2-黄, 3-绿, 4-蓝, 5-紫）与运营人员内部备注信息")
    @PutMapping("/{id}/remark")
    @RequirePermission("order:remark")
    public CommonResult<Void> updateOrderRemark(@PathVariable("id") Long id,
                                                @Valid @RequestBody OrderAdminRemarkDTO remarkDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (remarkDTO.getAdminFlag() != null) {
            order.setAdminFlag(remarkDTO.getAdminFlag());
        }
        if (remarkDTO.getAdminRemark() != null) {
            order.setAdminRemark(remarkDTO.getAdminRemark());
        }
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        return CommonResult.success(null, "运营备注保存成功");
    }

    /**
     * 查询订单流转时间轴日志
     */
    @Operation(summary = "查询订单流转时间轴日志", description = "获取指定订单各关键流转节点的操作时间、动作、经手人与明细")
    @GetMapping("/{id}/logs")
    @RequirePermission("order:view")
    public CommonResult<List<OrderLogVO>> getOrderLogs(@PathVariable("id") Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return CommonResult.success(buildOrderTimeline(order));
    }

    private List<OrderLogVO> buildOrderTimeline(Order order) {
        List<OrderLogVO> logs = new ArrayList<>();

        if (order.getCreateTime() != null) {
            logs.add(OrderLogVO.builder()
                    .time(order.getCreateTime())
                    .action("订单提交成功")
                    .operator("买家")
                    .detail("订单号: " + order.getOrderSn() + "，应付总额: ￥" + order.getTotalAmount())
                    .build());
        }

        if (order.getPaymentTime() != null) {
            String payTypeName = switch (order.getPayType() != null ? order.getPayType() : 0) {
                case 1 -> "支付宝支付";
                case 2 -> "微信支付";
                case 3 -> "银联支付";
                case 4 -> "余额支付";
                default -> "在线支付";
            };
            logs.add(OrderLogVO.builder()
                    .time(order.getPaymentTime())
                    .action("买家付款成功")
                    .operator("买家 / 支付平台")
                    .detail(payTypeName + "，实付金额: ￥" + order.getPayAmount() + (order.getTradeNo() != null ? "，交易流水号: " + order.getTradeNo() : ""))
                    .build());
        }

        if (order.getDeliveryTime() != null) {
            logs.add(OrderLogVO.builder()
                    .time(order.getDeliveryTime())
                    .action("商家订单发货")
                    .operator("平台运营 / 商家")
                    .detail("承运快递: " + (order.getDeliveryCompany() != null ? order.getDeliveryCompany() : "默认快递") + "，运单号: " + order.getDeliverySn())
                    .build());
        }

        if (order.getReceiveTime() != null) {
            logs.add(OrderLogVO.builder()
                    .time(order.getReceiveTime())
                    .action("确认收货已完成")
                    .operator("买家 / 系统确认")
                    .detail("订单交易顺利完成")
                    .build());
        }

        if (order.getCancelTime() != null) {
            logs.add(OrderLogVO.builder()
                    .time(order.getCancelTime())
                    .action("订单已取消")
                    .operator("买家 / 运营强制关闭")
                    .detail("取消原因: " + (order.getCancelReason() != null ? order.getCancelReason() : "无"))
                    .build());
        }

        logs.sort(Comparator.comparing(OrderLogVO::getTime));
        return logs;
    }
}
