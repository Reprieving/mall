package com.example.baseboot.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.baseboot.module.dashboard.service.AdminDashboardService;
import com.example.baseboot.module.dashboard.vo.*;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.entity.OrderItem;
import com.example.baseboot.module.order.mapper.OrderItemMapper;
import com.example.baseboot.module.order.mapper.OrderMapper;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.mapper.CategoryMapper;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.mapper.UserCertificationMapper;
import com.example.baseboot.module.user.profile.entity.SysUser;
import com.example.baseboot.module.user.profile.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营控制台数据统计大屏服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SysUserMapper sysUserMapper;
    private final ShopMapper shopMapper;
    private final SpuMapper spuMapper;
    private final UserCertificationMapper userCertificationMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public DashboardOverviewVO getOverview() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 今日订单统计
        List<Order> todayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .ne(Order::getStatus, 4)); // 排除已取消

        BigDecimal todayGmv = todayOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayPayAmount = todayOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() >= 1 && o.getStatus() <= 3)
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int todayOrderCount = todayOrders.size();

        // 今日新增买家数
        Long todayNewUsers = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .ge(SysUser::getCreateTime, todayStart));

        // 今日客单价
        BigDecimal todayAov = todayOrderCount > 0 ? todayGmv.divide(BigDecimal.valueOf(todayOrderCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 全平台累计数据
        Long totalUsers = sysUserMapper.selectCount(null);
        Long totalShops = shopMapper.selectCount(new LambdaQueryWrapper<Shop>().in(Shop::getStatus, 1, 2));
        Long totalProducts = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getStatus, 1));
        Long totalOrders = orderMapper.selectCount(null);

        List<Order> allValidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>().ne(Order::getStatus, 4));
        BigDecimal totalGmv = allValidOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardOverviewVO.builder()
                .todayGmv(todayGmv)
                .todayPayAmount(todayPayAmount)
                .todayOrderCount(todayOrderCount)
                .todayNewUsers(todayNewUsers != null ? todayNewUsers.intValue() : 0)
                .todayAov(todayAov)
                .totalUsers(totalUsers)
                .totalShops(totalShops)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalGmv(totalGmv)
                .build();
    }

    @Override
    public DashboardTodosVO getTodos() {
        Long pendingCertCount = userCertificationMapper.selectCount(new LambdaQueryWrapper<UserCertification>()
                .eq(UserCertification::getStatus, 0));

        Long pendingShopCount = shopMapper.selectCount(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 0));

        Long pendingDeliverCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1));

        Long lowStockProductCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, 1)
                .le(Spu::getTotalStock, 10));

        return DashboardTodosVO.builder()
                .pendingCertCount(pendingCertCount != null ? pendingCertCount : 0L)
                .pendingShopCount(pendingShopCount != null ? pendingShopCount : 0L)
                .pendingDeliverCount(pendingDeliverCount != null ? pendingDeliverCount : 0L)
                .lowStockProductCount(lowStockProductCount != null ? lowStockProductCount : 0L)
                .build();
    }

    @Override
    public DashboardTrendVO getTrend(Integer days) {
        int range = (days == null || days <= 0 || days > 90) ? 7 : days;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(range - 1);

        LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, startTime)
                .ne(Order::getStatus, 4));

        Map<String, List<Order>> dateOrderMap = orders.stream()
                .filter(o -> o.getCreateTime() != null)
                .collect(Collectors.groupingBy(o -> o.getCreateTime().toLocalDate().toString()));

        List<String> dates = new ArrayList<>(range);
        List<BigDecimal> gmvList = new ArrayList<>(range);
        List<Integer> orderCountList = new ArrayList<>(range);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < range; i++) {
            LocalDate d = startDate.plusDays(i);
            String dateStr = d.format(dtf);
            dates.add(dateStr);

            List<Order> dayOrders = dateOrderMap.getOrDefault(dateStr, Collections.emptyList());
            BigDecimal dayGmv = dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            gmvList.add(dayGmv);
            orderCountList.add(dayOrders.size());
        }

        return DashboardTrendVO.builder()
                .dates(dates)
                .gmvList(gmvList)
                .orderCountList(orderCountList)
                .build();
    }

    @Override
    public List<TopProductVO> getTopProducts(Integer limit) {
        int max = (limit == null || limit <= 0 || limit > 50) ? 10 : limit;

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .orderByDesc(OrderItem::getId)
                .last("LIMIT 1000"));

        if (CollectionUtils.isEmpty(items)) {
            // 如果尚无订单，直接从 SPU 表返回展示
            List<Spu> spus = spuMapper.selectList(new LambdaQueryWrapper<Spu>()
                    .eq(Spu::getStatus, 1)
                    .orderByDesc(Spu::getMinPrice)
                    .last("LIMIT " + max));

            return spus.stream().map(s -> TopProductVO.builder()
                    .spuId(s.getId())
                    .spuName(s.getName())
                    .spuPic(s.getMainPic())
                    .salesCount(0)
                    .salesAmount(BigDecimal.ZERO)
                    .build()).collect(Collectors.toList());
        }

        Map<Long, List<OrderItem>> spuItemMap = items.stream().collect(Collectors.groupingBy(OrderItem::getSpuId));

        List<TopProductVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : spuItemMap.entrySet()) {
            Long spuId = entry.getKey();
            List<OrderItem> itemList = entry.getValue();

            String spuName = itemList.get(0).getSpuName();
            String spuPic = itemList.get(0).getSpuPic();

            int salesCount = itemList.stream().mapToInt(i -> i.getQuantity() != null ? i.getQuantity() : 0).sum();
            BigDecimal salesAmount = itemList.stream()
                    .map(OrderItem::getSubtotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(TopProductVO.builder()
                    .spuId(spuId)
                    .spuName(spuName)
                    .spuPic(spuPic)
                    .salesCount(salesCount)
                    .salesAmount(salesAmount)
                    .build());
        }

        result.sort((a, b) -> b.getSalesAmount().compareTo(a.getSalesAmount()));
        if (result.size() > max) {
            return result.subList(0, max);
        }
        return result;
    }

    @Override
    public List<CategoryRatioVO> getCategoryRatio() {
        List<Category> firstLevelCategories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getLevel, 1)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort));

        if (CollectionUtils.isEmpty(firstLevelCategories)) {
            return Collections.emptyList();
        }

        List<OrderItem> allItems = orderItemMapper.selectList(null);
        BigDecimal totalSales = allItems.stream()
                .map(OrderItem::getSubtotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryRatioVO> list = new ArrayList<>();
        for (Category cat : firstLevelCategories) {
            BigDecimal sales = totalSales.compareTo(BigDecimal.ZERO) > 0 ? totalSales.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            BigDecimal ratio = totalSales.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("50.00") : BigDecimal.ZERO;

            list.add(CategoryRatioVO.builder()
                    .categoryId(cat.getId())
                    .categoryName(cat.getName())
                    .salesAmount(sales)
                    .ratio(ratio)
                    .build());
        }

        return list;
    }
}
