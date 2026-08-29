package com.example.baseboot.module.admin.dashboard.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.admin.dashboard.service.AdminDashboardService;
import com.example.baseboot.module.admin.dashboard.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营端数据看板大屏控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 获取今日核心大盘指标概览
     */
    @GetMapping("/overview")
    @RequirePermission("dashboard:view")
    public CommonResult<DashboardOverviewVO> getOverview() {
        DashboardOverviewVO vo = adminDashboardService.getOverview();
        return CommonResult.success(vo);
    }

    /**
     * 获取运营待办事项角标数量
     */
    @GetMapping("/todos")
    @RequirePermission("dashboard:view")
    public CommonResult<DashboardTodosVO> getTodos() {
        DashboardTodosVO vo = adminDashboardService.getTodos();
        return CommonResult.success(vo);
    }

    /**
     * 获取销售走势与订单量趋势图表数据
     */
    @GetMapping("/trend")
    @RequirePermission("dashboard:view")
    public CommonResult<DashboardTrendVO> getTrend(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        DashboardTrendVO vo = adminDashboardService.getTrend(days);
        return CommonResult.success(vo);
    }

    /**
     * 获取热销商品 Top 榜单
     */
    @GetMapping("/top-products")
    @RequirePermission("dashboard:view")
    public CommonResult<List<TopProductVO>> getTopProducts(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        List<TopProductVO> list = adminDashboardService.getTopProducts(limit);
        return CommonResult.success(list);
    }

    /**
     * 获取各品类销售占比
     */
    @GetMapping("/category-ratio")
    @RequirePermission("dashboard:view")
    public CommonResult<List<CategoryRatioVO>> getCategoryRatio() {
        List<CategoryRatioVO> list = adminDashboardService.getCategoryRatio();
        return CommonResult.success(list);
    }
}
