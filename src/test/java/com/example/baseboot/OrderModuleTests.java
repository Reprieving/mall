package com.example.baseboot;

import com.example.baseboot.module.order.dto.OrderItemParamDTO;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.entity.OrderItem;
import com.example.baseboot.module.order.entity.OrderRefund;
import com.example.baseboot.module.order.vo.OrderDetailVO;
import com.example.baseboot.module.order.vo.OrderItemVO;
import com.example.baseboot.module.order.vo.OrderPreviewVO;
import com.example.baseboot.module.order.vo.OrderVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class OrderModuleTests {

    @Test
    void testOrderAmountCalculation() {
        OrderItemParamDTO item1 = OrderItemParamDTO.builder().skuId(1L).quantity(2).build();
        OrderItemParamDTO item2 = OrderItemParamDTO.builder().skuId(2L).quantity(1).build();

        BigDecimal price1 = new BigDecimal("7999.00");
        BigDecimal price2 = new BigDecimal("8999.00");

        BigDecimal subtotal1 = price1.multiply(new BigDecimal(item1.getQuantity()));
        BigDecimal subtotal2 = price2.multiply(new BigDecimal(item2.getQuantity()));

        BigDecimal totalAmount = subtotal1.add(subtotal2);
        BigDecimal freightAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.add(freightAmount);

        Assertions.assertEquals(new BigDecimal("15998.00"), subtotal1);
        Assertions.assertEquals(new BigDecimal("8999.00"), subtotal2);
        Assertions.assertEquals(new BigDecimal("24997.00"), totalAmount);
        Assertions.assertEquals(new BigDecimal("24997.00"), payAmount);
    }

    @Test
    void testOrderEntityToVOConversion() {
        Order order = Order.builder()
                .id(100L)
                .userId(1L)
                .orderSn("ORD202608291530001234")
                .totalAmount(new BigDecimal("7999.00"))
                .freightAmount(BigDecimal.ZERO)
                .payAmount(new BigDecimal("7999.00"))
                .payType(2)
                .status(1) // 待发货
                .receiverName("张伟")
                .receiverPhone("13800138000")
                .receiverProvince("北京市")
                .receiverCity("北京市")
                .receiverDistrict("海淀区")
                .receiverDetailAddress("中关村南大街1号院")
                .createTime(LocalDateTime.now())
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .orderId(100L)
                .orderSn("ORD202608291530001234")
                .spuId(1L)
                .spuName("Apple iPhone 15 Pro")
                .skuId(1L)
                .skuCode("SKU-IP15P-TIT-128")
                .skuName("Apple iPhone 15 Pro 原色钛金属 128GB")
                .skuPrice(new BigDecimal("7999.00"))
                .quantity(1)
                .subtotalAmount(new BigDecimal("7999.00"))
                .specData("[{\"specKeyName\":\"机身颜色\",\"specValue\":\"原色钛金属\"}]")
                .createTime(LocalDateTime.now())
                .build();

        OrderVO orderVO = OrderVO.fromEntity(order);
        OrderItemVO itemVO = OrderItemVO.fromEntity(item);

        OrderDetailVO detailVO = OrderDetailVO.builder()
                .orderInfo(orderVO)
                .items(Collections.singletonList(itemVO))
                .build();

        Assertions.assertEquals("ORD202608291530001234", detailVO.getOrderInfo().getOrderSn());
        Assertions.assertEquals(1, detailVO.getOrderInfo().getStatus());
        Assertions.assertEquals(1, detailVO.getItems().size());
        Assertions.assertEquals("SKU-IP15P-TIT-128", detailVO.getItems().get(0).getSkuCode());
        Assertions.assertEquals(new BigDecimal("7999.00"), detailVO.getItems().get(0).getSubtotalAmount());
    }

    @Test
    void testOrderPreviewVO() {
        OrderItemVO itemVO = OrderItemVO.builder()
                .skuId(1L)
                .skuName("Apple iPhone 15 Pro 原色钛金属 128GB")
                .skuPrice(new BigDecimal("7999.00"))
                .quantity(2)
                .subtotalAmount(new BigDecimal("15998.00"))
                .build();

        OrderPreviewVO previewVO = OrderPreviewVO.builder()
                .totalAmount(new BigDecimal("15998.00"))
                .freightAmount(BigDecimal.ZERO)
                .payAmount(new BigDecimal("15998.00"))
                .totalQuantity(2)
                .items(Collections.singletonList(itemVO))
                .build();

        Assertions.assertEquals(2, previewVO.getTotalQuantity());
        Assertions.assertEquals(new BigDecimal("15998.00"), previewVO.getPayAmount());
    }

    @Test
    void testOrderRefundEntityToVOConversion() {
        OrderRefund refund = OrderRefund.builder()
                .id(1L)
                .refundSn("REF202609121800001234")
                .orderId(100L)
                .orderSn("ORD202608291530001234")
                .shopId(1L)
                .userId(1L)
                .refundType(1)
                .refundAmount(new BigDecimal("7999.00"))
                .reason("不想要了 / 协商一致退款")
                .description("包装未拆封")
                .proofPics("https://example.com/pic1.jpg,https://example.com/pic2.jpg")
                .status(0) // 待审核
                .createTime(LocalDateTime.now())
                .build();

        com.example.baseboot.module.order.vo.OrderRefundVO vo =
                com.example.baseboot.module.order.vo.OrderRefundVO.fromEntity(refund);

        Assertions.assertNotNull(vo);
        Assertions.assertEquals("REF202609121800001234", vo.getRefundSn());
        Assertions.assertEquals("仅退款", vo.getRefundTypeDesc());
        Assertions.assertEquals("待审核", vo.getStatusDesc());
        Assertions.assertEquals(new BigDecimal("7999.00"), vo.getRefundAmount());
        Assertions.assertEquals("不想要了 / 协商一致退款", vo.getReason());
    }

    @Test
    void testOrderRefundAuditFlowVOStatus() {
        // 审核通过场景
        OrderRefund approvedRefund = OrderRefund.builder()
                .id(2L)
                .refundSn("REF202609121800005678")
                .orderId(101L)
                .orderSn("ORD202608291530005678")
                .shopId(1L)
                .userId(1L)
                .refundType(2)
                .refundAmount(new BigDecimal("8999.00"))
                .reason("商品质量问题")
                .status(1) // 审核通过
                .auditTime(LocalDateTime.now())
                .auditUserId(1L)
                .auditUserName("admin")
                .auditRemark("同意退货退款，仓库已验货")
                .createTime(LocalDateTime.now().minusHours(1))
                .build();

        com.example.baseboot.module.order.vo.OrderRefundVO approvedVO =
                com.example.baseboot.module.order.vo.OrderRefundVO.fromEntity(approvedRefund);
        Assertions.assertEquals("退货退款", approvedVO.getRefundTypeDesc());
        Assertions.assertEquals("审核通过", approvedVO.getStatusDesc());
        Assertions.assertEquals("admin", approvedVO.getAuditUserName());
        Assertions.assertEquals("同意退货退款，仓库已验货", approvedVO.getAuditRemark());

        // 审核驳回场景
        OrderRefund rejectedRefund = OrderRefund.builder()
                .id(3L)
                .refundSn("REF202609121800009999")
                .orderId(102L)
                .orderSn("ORD202608291530009999")
                .shopId(1L)
                .userId(1L)
                .refundType(1)
                .refundAmount(new BigDecimal("100.00"))
                .reason("七天无理由退货")
                .status(2) // 审核驳回
                .auditTime(LocalDateTime.now())
                .auditUserId(1L)
                .auditUserName("admin")
                .auditRemark("商品已被严重损坏，不符合退货标准")
                .createTime(LocalDateTime.now().minusDays(1))
                .build();

        com.example.baseboot.module.order.vo.OrderRefundVO rejectedVO =
                com.example.baseboot.module.order.vo.OrderRefundVO.fromEntity(rejectedRefund);
        Assertions.assertEquals("审核驳回", rejectedVO.getStatusDesc());
        Assertions.assertEquals("商品已被严重损坏，不符合退货标准", rejectedVO.getAuditRemark());
    }

    @Test
    void testOrderDetailWithRefundInfoEmbedding() {
        Order order = Order.builder()
                .id(100L)
                .userId(1L)
                .orderSn("ORD202608291530001234")
                .status(1) // 待发货
                .totalAmount(new BigDecimal("7999.00"))
                .payAmount(new BigDecimal("7999.00"))
                .build();

        com.example.baseboot.module.order.vo.OrderRefundVO refundVO =
                com.example.baseboot.module.order.vo.OrderRefundVO.builder()
                        .id(1L)
                        .refundSn("REF202609121800001234")
                        .status(0)
                        .statusDesc("待审核")
                        .refundAmount(new BigDecimal("7999.00"))
                        .reason("买错了配置")
                        .build();

        OrderDetailVO detailVO = OrderDetailVO.builder()
                .orderInfo(OrderVO.fromEntity(order))
                .refundInfo(refundVO)
                .build();

        Assertions.assertNotNull(detailVO.getRefundInfo());
        Assertions.assertEquals("REF202609121800001234", detailVO.getRefundInfo().getRefundSn());
        Assertions.assertEquals("待审核", detailVO.getRefundInfo().getStatusDesc());
        Assertions.assertEquals("买错了配置", detailVO.getRefundInfo().getReason());
    }
}
