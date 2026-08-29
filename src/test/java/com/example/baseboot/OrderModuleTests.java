package com.example.baseboot;

import com.example.baseboot.module.order.dto.OrderItemParamDTO;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.entity.OrderItem;
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
}
