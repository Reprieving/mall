package com.example.baseboot.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.order.dto.OrderRefundApplyDTO;
import com.example.baseboot.module.order.dto.OrderRefundAuditDTO;
import com.example.baseboot.module.order.dto.OrderRefundQueryDTO;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.entity.OrderItem;
import com.example.baseboot.module.order.entity.OrderRefund;
import com.example.baseboot.module.order.mapper.OrderItemMapper;
import com.example.baseboot.module.order.mapper.OrderMapper;
import com.example.baseboot.module.order.mapper.OrderRefundMapper;
import com.example.baseboot.module.order.service.OrderRefundService;
import com.example.baseboot.module.order.service.OrderService;
import com.example.baseboot.module.order.vo.OrderRefundVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单退款申请与审批业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderRefundServiceImpl extends ServiceImpl<OrderRefundMapper, OrderRefund> implements OrderRefundService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SpuMapper spuMapper;
    private final ShopMapper shopMapper;

    @Lazy
    private final OrderService orderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefundVO applyRefund(Long userId, Long orderId, OrderRefundApplyDTO applyDTO) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        if (applyDTO == null || !StringUtils.hasText(applyDTO.getReason())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "退款原因不能为空");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleteStatus() == 1) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        // 校验订单归属
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ORDER_FORBIDDEN);
        }

        // 校验订单状态: 未付款不允许申请退款 (应走取消订单); 已取消/已关闭不允许申请退款
        if (order.getStatus() == 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "未付款订单无需申请退款，请直接取消订单");
        }
        if (order.getStatus() == 4) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "该订单已取消，无法申请退款");
        }
        if (order.getStatus() == 5) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "该订单已关闭，无法申请退款");
        }

        // 校验是否存在处于待审核状态的退款记录 (防重复申请)
        Long pendingCount = this.count(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getOrderId, orderId)
                .eq(OrderRefund::getStatus, 0));
        if (pendingCount > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "该订单已有退款申请正在处理中，请勿重复申请");
        }

        // 校验退款金额
        BigDecimal refundAmount = applyDTO.getRefundAmount();
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            refundAmount = order.getPayAmount();
        } else if (refundAmount.compareTo(order.getPayAmount()) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "申请退款金额不能超过订单实付金额: ￥" + order.getPayAmount());
        }

        // 获取关联店铺 shopId (通过订单条目所关联的 SPU 获取)
        Long shopId = 1L;
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        if (!CollectionUtils.isEmpty(items) && items.get(0).getSpuId() != null) {
            Spu spu = spuMapper.selectById(items.get(0).getSpuId());
            if (spu != null && spu.getShopId() != null) {
                shopId = spu.getShopId();
            }
        }

        // 生成退款流水号
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        String refundSn = "REF" + timestamp + random;

        OrderRefund refund = OrderRefund.builder()
                .refundSn(refundSn)
                .orderId(orderId)
                .orderSn(order.getOrderSn())
                .shopId(shopId)
                .userId(order.getUserId())
                .refundType(applyDTO.getRefundType() != null ? applyDTO.getRefundType() : 1)
                .refundAmount(refundAmount)
                .reason(applyDTO.getReason().trim())
                .description(applyDTO.getDescription())
                .proofPics(applyDTO.getProofPics())
                .status(0) // 0-待审核
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(refund);

        Shop shop = shopMapper.selectById(shopId);
        OrderRefundVO vo = OrderRefundVO.fromEntity(refund);
        if (shop != null) {
            vo.setShopName(shop.getName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefundVO auditRefund(Long auditUserId, String auditUserName, Long refundId, OrderRefundAuditDTO auditDTO) {
        if (refundId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "退款申请ID不能为空");
        }
        if (auditDTO == null || auditDTO.getStatus() == null || (auditDTO.getStatus() != 1 && auditDTO.getStatus() != 2)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "审批状态非法 (1-审核通过, 2-审核驳回)");
        }

        OrderRefund refund = this.getById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "退款申请记录不存在");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "该退款申请已完成审批，无法重复操作");
        }

        if (auditDTO.getStatus() == 1) {
            // 审核通过: 调用关单并释放库存，将订单流转为 5-已关闭
            String closeReason = "买家申请售后退款通过: " + (StringUtils.hasText(auditDTO.getAuditRemark()) ? auditDTO.getAuditRemark() : refund.getReason());
            orderService.closeOrder(refund.getOrderId(), closeReason);
            refund.setStatus(1); // 1-审核通过
        } else {
            // 审核驳回: 记录驳回原因，订单保持原流转状态
            if (!StringUtils.hasText(auditDTO.getAuditRemark())) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "审核驳回时必须填写驳回原因说明");
            }
            refund.setStatus(2); // 2-审核驳回
        }

        refund.setAuditTime(LocalDateTime.now());
        refund.setAuditUserId(auditUserId);
        refund.setAuditUserName(auditUserName != null ? auditUserName : "运营审核员");
        refund.setAuditRemark(auditDTO.getAuditRemark());
        refund.setUpdateTime(LocalDateTime.now());
        this.updateById(refund);

        Shop shop = shopMapper.selectById(refund.getShopId());
        OrderRefundVO vo = OrderRefundVO.fromEntity(refund);
        if (shop != null) {
            vo.setShopName(shop.getName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderRefundVO auditRefundByOrderId(Long auditUserId, String auditUserName, Long orderId, OrderRefundAuditDTO auditDTO) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        OrderRefund refund = this.getOne(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getOrderId, orderId)
                .eq(OrderRefund::getStatus, 0)
                .orderByDesc(OrderRefund::getId)
                .last("LIMIT 1"));
        if (refund == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "该订单暂无待处理的退款申请记录");
        }
        return auditRefund(auditUserId, auditUserName, refund.getId(), auditDTO);
    }

    @Override
    public OrderRefundVO getRefundByOrderId(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "订单ID不能为空");
        }
        OrderRefund refund = this.getOne(new LambdaQueryWrapper<OrderRefund>()
                .eq(OrderRefund::getOrderId, orderId)
                .orderByDesc(OrderRefund::getId)
                .last("LIMIT 1"));
        if (refund == null) {
            return null;
        }
        Shop shop = shopMapper.selectById(refund.getShopId());
        OrderRefundVO vo = OrderRefundVO.fromEntity(refund);
        if (shop != null) {
            vo.setShopName(shop.getName());
        }
        return vo;
    }

    @Override
    public OrderRefundVO getRefundById(Long refundId) {
        if (refundId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "退款申请ID不能为空");
        }
        OrderRefund refund = this.getById(refundId);
        if (refund == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "退款申请记录不存在");
        }
        Shop shop = shopMapper.selectById(refund.getShopId());
        OrderRefundVO vo = OrderRefundVO.fromEntity(refund);
        if (shop != null) {
            vo.setShopName(shop.getName());
        }
        return vo;
    }

    @Override
    public CommonPage<OrderRefundVO> pageRefunds(OrderRefundQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new OrderRefundQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<OrderRefund> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderRefund> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getOrderId() != null) {
            wrapper.eq(OrderRefund::getOrderId, queryDTO.getOrderId());
        }
        if (StringUtils.hasText(queryDTO.getOrderSn())) {
            wrapper.like(OrderRefund::getOrderSn, queryDTO.getOrderSn().trim());
        }
        if (StringUtils.hasText(queryDTO.getRefundSn())) {
            wrapper.like(OrderRefund::getRefundSn, queryDTO.getRefundSn().trim());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(OrderRefund::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getShopId() != null) {
            wrapper.eq(OrderRefund::getShopId, queryDTO.getShopId());
        }
        if (queryDTO.getUserId() != null) {
            wrapper.eq(OrderRefund::getUserId, queryDTO.getUserId());
        }

        wrapper.orderByDesc(OrderRefund::getId);
        Page<OrderRefund> refundPage = this.page(page, wrapper);

        if (CollectionUtils.isEmpty(refundPage.getRecords())) {
            return CommonPage.restPage(refundPage, Collections.emptyList());
        }

        // 批量查询店铺名称
        Set<Long> shopIds = refundPage.getRecords().stream()
                .map(OrderRefund::getShopId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> shopNameMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            shopNameMap = shops.stream().collect(Collectors.toMap(Shop::getId, Shop::getName, (k1, k2) -> k1));
        }

        Map<Long, String> finalShopNameMap = shopNameMap;
        List<OrderRefundVO> voList = refundPage.getRecords().stream().map(r -> {
            OrderRefundVO vo = OrderRefundVO.fromEntity(r);
            vo.setShopName(finalShopNameMap.get(r.getShopId()));
            return vo;
        }).collect(Collectors.toList());

        return CommonPage.restPage(refundPage, voList);
    }
}
