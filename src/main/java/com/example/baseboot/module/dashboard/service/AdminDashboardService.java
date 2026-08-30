package com.example.baseboot.module.dashboard.service;

import com.example.baseboot.module.dashboard.vo.*;

import java.util.List;

/**
 * 运营控制台数据统计大屏服务接口
 */
public interface AdminDashboardService {

    /**
     * 获取今日核心大盘指标与累计数据
     */
    DashboardOverviewVO getOverview();

    /**
     * 获取待办事项角标数量
     */
    DashboardTodosVO getTodos();

    /**
     * 获取销售额与订单量趋势
     * @param days 天数 (7 或 30)
     */
    DashboardTrendVO getTrend(Integer days);

    /**
     * 获取热销商品 Top 榜单
     */
    List<TopProductVO> getTopProducts(Integer limit);

    /**
     * 获取各品类销售占比
     */
    List<CategoryRatioVO> getCategoryRatio();
}
