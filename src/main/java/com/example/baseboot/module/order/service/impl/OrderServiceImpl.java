package com.example.baseboot.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.order.dto.*;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.entity.OrderItem;
import com.example.baseboot.module.order.mapper.OrderItemMapper;
import com.example.baseboot.module.order.mapper.OrderMapper;
import com.example.baseboot.module.order.service.OrderService;
import com.example.baseboot.module.order.vo.OrderDetailVO;
import com.example.baseboot.module.order.vo.OrderItemVO;
import com.example.baseboot.module.order.vo.OrderPreviewVO;
import com.example.baseboot.module.order.vo.OrderVO;
import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.sku.mapper.SkuMapper;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.product.spu.service.SpuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单核心业务实现类 (基于 Redisson 分布式锁防超卖与商品快照机制)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;
    private final SpuService spuService;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:order:sku:";

    @Override
    public OrderPreviewVO previewOrder(Long userId, OrderPreviewDTO previewDTO) {
        if (previewDTO == null || CollectionUtils.isEmpty(previewDTO.getItems())) {
            throw new BusinessException(ResultCode.ORDER_ITEM_EMPTY);
        }

        List<OrderItemVO> itemVOList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (OrderItemParamDTO param : previewDTO.getItems()) {
            if (param.getSkuId() == null || param.getQuantity() == null || param.getQuantity() <= 0) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "商品条目或数量非法");
            }

            Sku sku = skuMapper.selectById(param.getSkuId());
            if (sku == null || sku.getStatus() == 0) {
                throw new BusinessException(ResultCode.SKU_NOT_EXIST, "包含不可售的SKU商品: " + param.getSkuId());
            }

            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null || spu.getStatus() == 0) {
                throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF, "商品 [" + sku.getName() + "] 已下架");
            }

            if (sku.getStock() < param.getQuantity()) {
                throw new BusinessException(ResultCode.SKU_STOCK_NOT_ENOUGH, "商品 [" + sku.getName() + "] 库存不足，当前可用库存: " + sku.getStock());
            }

            BigDecimal subtotal = sku.getPrice().multiply(new BigDecimal(param.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            totalQuantity += param.getQuantity();

            OrderItemVO itemVO = OrderItemVO.builder()
                    .spuId(spu.getId())
                    .spuName(spu.getName())
                    .spuPic(spu.getMainPic())
                    .skuId(sku.getId())
                    .skuCode(sku.getSkuCode())
                    .skuName(sku.getName())
                    .skuPic(StringUtils.hasText(sku.getPic()) ? sku.getPic() : spu.getMainPic())
                    .skuPrice(sku.getPrice())
                    .quantity(param.getQuantity())
                    .subtotalAmount(subtotal)
                    .specData(sku.getSpecData())
                    .build();

            itemVOList.add(itemVO);
        }

        BigDecimal freightAmount = BigDecimal.ZERO; // 免运费/基础运费计算
        BigDecimal payAmount = totalAmount.add(freightAmount);

        return OrderPreviewVO.builder()
                .totalAmount(totalAmount)
                .freightAmount(freightAmount)
                .payAmount(payAmount)
                .totalQuantity(totalQuantity)
                .items(itemVOList)
                .build();
    }

    @Override
    public OrderDetailVO createOrder(Long userId, OrderCreateDTO createDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (createDTO == null || CollectionUtils.isEmpty(createDTO.getItems())) {
            throw new BusinessException(ResultCode.ORDER_ITEM_EMPTY);
        }
        if (!StringUtils.hasText(createDTO.getReceiverName()) || !StringUtils.hasText(createDTO.getReceiverPhone())
                || !StringUtils.hasText(createDTO.getReceiverDetailAddress())) {
            throw new BusinessException(ResultCode.RECEIVER_INFO_EMPTY);
        }

        // 1. 获取涉及的所有 SKU ID，并进行自然排序以避免死锁
        List<Long> sortedSkuIds = createDTO.getItems().stream()
                .map(OrderItemParamDTO::getSkuId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<RLock> acquiredLocks = new ArrayList<>();

        try {
            // 2. 加分布式锁
            for (Long skuId : sortedSkuIds) {
                RLock lock = redissonClient.getLock(LOCK_PREFIX + skuId);
                boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
                if (!isLocked) {
                    throw new BusinessException(ResultCode.FAILED, "当前下单人数较多，请稍后重试");
                }
                acquiredLocks.add(lock);
            }

            // 3. 在事务中执行库存扣减与订单落库
            return doCreateOrderInTransaction(userId, createDTO);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("下单获取分布式锁被中断", e);
            throw new BusinessException(ResultCode.FAILED, "下单排队超时，请重试");
        } finally {
            // 4. 释放分布式锁
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO doCreateOrderInTransaction(Long userId, OrderCreateDTO createDTO) {
        String orderSn = generateOrderSn();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        Set<Long> affectedSpuIds = new HashSet<>();

        for (OrderItemParamDTO itemParam : createDTO.getItems()) {
            Sku sku = skuMapper.selectById(itemParam.getSkuId());
            if (sku == null || sku.getStatus() == 0) {
                throw new BusinessException(ResultCode.SKU_NOT_EXIST, "包含不可售的SKU商品");
            }

            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null || spu.getStatus() == 0) {
                throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF, "商品 [" + sku.getName() + "] 已下架");
            }

            if (sku.getStock() < itemParam.getQuantity()) {
                throw new BusinessException(ResultCode.SKU_STOCK_NOT_ENOUGH, "商品 [" + sku.getName() + "] 库存不足，当前可用库存: " + sku.getStock());
            }

            // 扣减物理库存
            sku.setStock(sku.getStock() - itemParam.getQuantity());
            sku.setUpdateTime(LocalDateTime.now());
            skuMapper.updateById(sku);

            affectedSpuIds.add(spu.getId());

            BigDecimal subtotal = sku.getPrice().multiply(new BigDecimal(itemParam.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            // 构建订单明细快照
            OrderItem orderItem = OrderItem.builder()
                    .orderSn(orderSn)
                    .spuId(spu.getId())
                    .spuName(spu.getName())
                    .spuPic(spu.getMainPic())
                    .skuId(sku.getId())
                    .skuCode(sku.getSkuCode())
                    .skuName(sku.getName())
                    .skuPic(StringUtils.hasText(sku.getPic()) ? sku.getPic() : spu.getMainPic())
                    .skuPrice(sku.getPrice())
                    .quantity(itemParam.getQuantity())
                    .subtotalAmount(subtotal)
                    .specData(sku.getSpecData())
                    .createTime(LocalDateTime.now())
                    .build();

            orderItems.add(orderItem);
        }

        // 联动重算受影响 SPU 的总库存与最低/最高价
        for (Long spuId : affectedSpuIds) {
            spuService.recalculateSpuStockAndPrice(spuId);
        }

        BigDecimal freightAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.add(freightAmount);
        int payType = createDTO.getPayType() != null ? createDTO.getPayType() : 0;

        // 保存订单主表
        Order order = Order.builder()
                .userId(userId)
                .orderSn(orderSn)
                .totalAmount(totalAmount)
                .freightAmount(freightAmount)
                .payAmount(payAmount)
                .payType(payType)
                .status(0) // 0-待付款
                .receiverName(createDTO.getReceiverName().trim())
                .receiverPhone(createDTO.getReceiverPhone().trim())
                .receiverProvince(createDTO.getReceiverProvince())
                .receiverCity(createDTO.getReceiverCity())
                .receiverDistrict(createDTO.getReceiverDistrict())
                .receiverDetailAddress(createDTO.getReceiverDetailAddress().trim())
                .note(createDTO.getNote())
                .deleteStatus(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(order);

        // 批量保存订单商品快照
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        OrderVO orderVO = OrderVO.fromEntity(order);
        orderVO.setTotalQuantity(orderItems.stream().mapToInt(OrderItem::getQuantity).sum());
        orderVO.setMainPic(!orderItems.isEmpty() ? orderItems.get(0).getSkuPic() : null);

        List<OrderItemVO> itemVOList = orderItems.stream().map(OrderItemVO::fromEntity).collect(Collectors.toList());

        return OrderDetailVO.builder()
                .orderInfo(orderVO)
                .items(itemVOList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO payOrder(Long userId, Long orderId, OrderPayDTO payDTO) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_FORBIDDEN);
        }

        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有待付款的订单才允许支付");
        }

        int payType = payDTO.getPayType() != null ? payDTO.getPayType() : 1;
        String tradeNo = StringUtils.hasText(payDTO.getTradeNo()) ? payDTO.getTradeNo().trim() : generateTradeNo();

        order.setStatus(1); // 1-待发货 (已支付)
        order.setPayType(payType);
        order.setTradeNo(tradeNo);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);
        return getOrderDetail(userId, orderId, false);
    }

    @Override
    public OrderDetailVO cancelOrder(Long userId, Long orderId, OrderCancelDTO cancelDTO) {
        return doCancelOrder(userId, orderId, cancelDTO, false);
    }

    @Override
    public OrderDetailVO adminCancelOrder(Long orderId, OrderCancelDTO cancelDTO) {
        return doCancelOrder(null, orderId, cancelDTO, true);
    }

    private OrderDetailVO doCancelOrder(Long userId, Long orderId, OrderCancelDTO cancelDTO, boolean isAdmin) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!isAdmin && userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_FORBIDDEN);
        }

        // 仅待付款或待发货状态允许取消
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_CANCEL_FAILED, "当前订单状态不可取消");
        }

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        List<Long> sortedSkuIds = items.stream().map(OrderItem::getSkuId).distinct().sorted().collect(Collectors.toList());

        List<RLock> locks = new ArrayList<>();
        try {
            for (Long skuId : sortedSkuIds) {
                RLock lock = redissonClient.getLock(LOCK_PREFIX + skuId);
                lock.lock(10, TimeUnit.SECONDS);
                locks.add(lock);
            }

            // 执行回滚库存
            executeCancelInTransaction(order, items, cancelDTO);

        } finally {
            for (RLock lock : locks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        return getOrderDetail(userId, orderId, isAdmin);
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeCancelInTransaction(Order order, List<OrderItem> items, OrderCancelDTO cancelDTO) {
        Set<Long> affectedSpuIds = new HashSet<>();

        // 回滚库存
        for (OrderItem item : items) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                sku.setUpdateTime(LocalDateTime.now());
                skuMapper.updateById(sku);
                affectedSpuIds.add(sku.getSpuId());
            }
        }

        // 重新同步 SPU 总库存
        for (Long spuId : affectedSpuIds) {
            spuService.recalculateSpuStockAndPrice(spuId);
        }

        String reason = cancelDTO != null && StringUtils.hasText(cancelDTO.getCancelReason()) ? cancelDTO.getCancelReason().trim() : "用户主动取消";
        order.setStatus(4); // 4-已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        order.setUpdateTime(LocalDateTime.now());

        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO receiveOrder(Long userId, Long orderId) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_FORBIDDEN);
        }

        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有已发货的订单才能确认收货");
        }

        order.setStatus(3); // 3-已完成
        order.setReceiveTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        return getOrderDetail(userId, orderId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrder(Long userId, Long orderId) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_FORBIDDEN);
        }

        // 仅已完成(3)、已取消(4)、已关闭(5)允许逻辑删除
        if (order.getStatus() != 3 && order.getStatus() != 4 && order.getStatus() != 5) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_DELETE);
        }

        order.setDeleteStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId, boolean isAdmin) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || (!isAdmin && order.getDeleteStatus() == 1)) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (!isAdmin && userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_FORBIDDEN);
        }

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId));

        OrderVO orderVO = OrderVO.fromEntity(order);
        orderVO.setTotalQuantity(items.stream().mapToInt(OrderItem::getQuantity).sum());
        orderVO.setMainPic(!items.isEmpty() ? items.get(0).getSkuPic() : null);

        List<OrderItemVO> itemVOList = items.stream().map(OrderItemVO::fromEntity).collect(Collectors.toList());

        return OrderDetailVO.builder()
                .orderInfo(orderVO)
                .items(itemVOList)
                .build();
    }

    @Override
    public CommonPage<OrderVO> pageUserOrders(Long userId, OrderQueryDTO queryDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (queryDTO == null) {
            queryDTO = new OrderQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getDeleteStatus, 0);

        if (queryDTO.getStatus() != null) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getOrderSn())) {
            wrapper.eq(Order::getOrderSn, queryDTO.getOrderSn().trim());
        }
        wrapper.orderByDesc(Order::getId);

        Page<Order> orderPage = this.page(page, wrapper);
        return buildCommonPage(orderPage);
    }

    @Override
    public CommonPage<OrderVO> pageAdminOrders(OrderQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new OrderQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(Order::getUserId, queryDTO.getUserId());
        }
        if (StringUtils.hasText(queryDTO.getOrderSn())) {
            wrapper.eq(Order::getOrderSn, queryDTO.getOrderSn().trim());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getReceiverName())) {
            wrapper.like(Order::getReceiverName, queryDTO.getReceiverName().trim());
        }
        if (StringUtils.hasText(queryDTO.getReceiverPhone())) {
            wrapper.like(Order::getReceiverPhone, queryDTO.getReceiverPhone().trim());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(Order::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(Order::getCreateTime, queryDTO.getEndTime());
        }
        wrapper.orderByDesc(Order::getId);

        Page<Order> orderPage = this.page(page, wrapper);
        return buildCommonPage(orderPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO deliveryOrder(Long orderId, OrderDeliveryDTO deliveryDTO) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        if (deliveryDTO == null || !StringUtils.hasText(deliveryDTO.getDeliveryCompany()) || !StringUtils.hasText(deliveryDTO.getDeliverySn())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "物流公司及单号不能为空");
        }

        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "只有待发货状态的订单才允许发货操作");
        }

        order.setStatus(2); // 2-已发货
        order.setDeliveryCompany(deliveryDTO.getDeliveryCompany().trim());
        order.setDeliverySn(deliveryDTO.getDeliverySn().trim());
        order.setDeliveryTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        return getOrderDetail(null, orderId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO updateReceiverInfo(Long orderId, OrderReceiverUpdateDTO updateDTO) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 已发货或已完成的订单不允许修改收货地址
        if (order.getStatus() >= 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "已发货或已完成的订单无法修改收货地址");
        }

        order.setReceiverName(updateDTO.getReceiverName().trim());
        order.setReceiverPhone(updateDTO.getReceiverPhone().trim());
        if (updateDTO.getReceiverProvince() != null) {
            order.setReceiverProvince(updateDTO.getReceiverProvince());
        }
        if (updateDTO.getReceiverCity() != null) {
            order.setReceiverCity(updateDTO.getReceiverCity());
        }
        if (updateDTO.getReceiverDistrict() != null) {
            order.setReceiverDistrict(updateDTO.getReceiverDistrict());
        }
        order.setReceiverDetailAddress(updateDTO.getReceiverDetailAddress().trim());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);

        return getOrderDetail(null, orderId, true);
    }

    @Override
    public OrderDetailVO closeOrder(Long orderId, String closeReason) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        List<Long> sortedSkuIds = items.stream().map(OrderItem::getSkuId).distinct().sorted().collect(Collectors.toList());

        List<RLock> locks = new ArrayList<>();
        try {
            for (Long skuId : sortedSkuIds) {
                RLock lock = redissonClient.getLock(LOCK_PREFIX + skuId);
                lock.lock(10, TimeUnit.SECONDS);
                locks.add(lock);
            }

            executeCloseInTransaction(order, items, closeReason);

        } finally {
            for (RLock lock : locks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        return getOrderDetail(null, orderId, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeCloseInTransaction(Order order, List<OrderItem> items, String closeReason) {
        Set<Long> affectedSpuIds = new HashSet<>();

        // 退回库存
        for (OrderItem item : items) {
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                sku.setStock(sku.getStock() + item.getQuantity());
                sku.setUpdateTime(LocalDateTime.now());
                skuMapper.updateById(sku);
                affectedSpuIds.add(sku.getSpuId());
            }
        }

        for (Long spuId : affectedSpuIds) {
            spuService.recalculateSpuStockAndPrice(spuId);
        }

        order.setStatus(5); // 5-已关闭
        order.setCancelReason(StringUtils.hasText(closeReason) ? closeReason.trim() : "售后/管理端关闭");
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
    }

    private CommonPage<OrderVO> buildCommonPage(Page<Order> orderPage) {
        if (CollectionUtils.isEmpty(orderPage.getRecords())) {
            return CommonPage.restPage(orderPage, Collections.emptyList());
        }

        List<Long> orderIds = orderPage.getRecords().stream().map(Order::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));

        Map<Long, List<OrderItem>> itemMap = allItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<OrderVO> voList = orderPage.getRecords().stream().map(o -> {
            OrderVO vo = OrderVO.fromEntity(o);
            List<OrderItem> orderItems = itemMap.getOrDefault(o.getId(), Collections.emptyList());
            vo.setTotalQuantity(orderItems.stream().mapToInt(OrderItem::getQuantity).sum());
            vo.setMainPic(!orderItems.isEmpty() ? orderItems.get(0).getSkuPic() : null);
            return vo;
        }).collect(Collectors.toList());

        return CommonPage.restPage(orderPage, voList);
    }

    private String generateOrderSn() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        return "ORD" + dateStr + random;
    }

    private String generateTradeNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        int random = new Random().nextInt(900000) + 100000;
        return "PAY" + dateStr + random;
    }
}
