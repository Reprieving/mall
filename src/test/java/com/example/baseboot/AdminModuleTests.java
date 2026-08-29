package com.example.baseboot;

import com.example.baseboot.module.admin.dashboard.vo.DashboardOverviewVO;
import com.example.baseboot.module.admin.dashboard.vo.DashboardTodosVO;
import com.example.baseboot.module.admin.dashboard.vo.TopProductVO;
import com.example.baseboot.module.admin.order.vo.OrderAdminDetailVO;
import com.example.baseboot.module.admin.order.vo.OrderLogVO;
import com.example.baseboot.module.admin.product.vo.StockWarningVO;
import com.example.baseboot.module.admin.system.entity.SysAdminUser;
import com.example.baseboot.module.admin.system.entity.SysRole;
import com.example.baseboot.module.admin.system.vo.AdminUserVO;
import com.example.baseboot.module.admin.system.vo.RoleVO;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.vo.OrderVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

class AdminModuleTests {

    @Test
    void testAdminRoleVOConversion() {
        SysRole role = SysRole.builder()
                .id(1L)
                .name("超级管理员")
                .code("SUPER_ADMIN")
                .description("平台最高管理权限")
                .permissions("[\"*:*:*\"]")
                .status(1)
                .createTime(LocalDateTime.now())
                .build();

        RoleVO vo = RoleVO.fromEntity(role);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals("超级管理员", vo.getName());
        Assertions.assertEquals("SUPER_ADMIN", vo.getCode());
        Assertions.assertNotNull(vo.getPermissions());
        Assertions.assertEquals(1, vo.getPermissions().size());
        Assertions.assertEquals("*:*:*", vo.getPermissions().get(0));
    }

    @Test
    void testAdminUserVOConversion() {
        SysAdminUser user = SysAdminUser.builder()
                .id(10L)
                .username("ops_zhang")
                .nickname("张运营")
                .avatar("https://example.com/avatar.jpg")
                .email("ops@mall.com")
                .phone("13900000000")
                .roleId(2L)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();

        AdminUserVO vo = AdminUserVO.fromEntity(user);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals("ops_zhang", vo.getUsername());
        Assertions.assertEquals("张运营", vo.getNickname());
        Assertions.assertEquals(1, vo.getStatus());
    }

    @Test
    void testDashboardOverviewVO() {
        DashboardOverviewVO vo = DashboardOverviewVO.builder()
                .todayGmv(new BigDecimal("15998.00"))
                .todayPayAmount(new BigDecimal("15998.00"))
                .todayOrderCount(2)
                .todayNewUsers(5)
                .todayAov(new BigDecimal("7999.00"))
                .totalUsers(100L)
                .totalShops(20L)
                .totalProducts(50L)
                .totalOrders(30L)
                .totalGmv(new BigDecimal("239970.00"))
                .build();

        Assertions.assertEquals(new BigDecimal("15998.00"), vo.getTodayGmv());
        Assertions.assertEquals(2, vo.getTodayOrderCount());
        Assertions.assertEquals(new BigDecimal("7999.00"), vo.getTodayAov());
    }

    @Test
    void testDashboardTodosVO() {
        DashboardTodosVO vo = DashboardTodosVO.builder()
                .pendingCertCount(3L)
                .pendingShopCount(2L)
                .pendingDeliverCount(5L)
                .lowStockProductCount(8L)
                .build();

        Assertions.assertEquals(3L, vo.getPendingCertCount());
        Assertions.assertEquals(2L, vo.getPendingShopCount());
        Assertions.assertEquals(5L, vo.getPendingDeliverCount());
        Assertions.assertEquals(8L, vo.getLowStockProductCount());
    }

    @Test
    void testOrderAdminRemarkAndFlag() {
        Order order = Order.builder()
                .id(1L)
                .orderSn("ORD202608290001")
                .totalAmount(new BigDecimal("7999.00"))
                .payAmount(new BigDecimal("7999.00"))
                .adminFlag(1) // 红旗
                .adminRemark("用户要求指定顺丰特快并做保价处理")
                .createTime(LocalDateTime.now())
                .build();

        OrderVO vo = OrderVO.fromEntity(order);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals(1, vo.getAdminFlag());
        Assertions.assertEquals("用户要求指定顺丰特快并做保价处理", vo.getAdminRemark());
    }

    @Test
    void testStockWarningVO() {
        StockWarningVO vo = StockWarningVO.builder()
                .spuId(101L)
                .spuName("Apple iPhone 15 Pro")
                .spuCode("SPU-IPHONE15P")
                .shopId(1L)
                .shopName("极光数码官方旗舰店")
                .totalStock(3)
                .minPrice(new BigDecimal("7999.00"))
                .status(1)
                .build();

        Assertions.assertEquals(101L, vo.getSpuId());
        Assertions.assertEquals(3, vo.getTotalStock());
        Assertions.assertEquals("极光数码官方旗舰店", vo.getShopName());
    }
}
