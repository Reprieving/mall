package com.example.baseboot.module.dashboard.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.dashboard.service.AdminDashboardService;
import com.example.baseboot.module.dashboard.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营端数据看板大屏控制器
 */
@Tag(name = "03. 运营数据大盘 (AdminDashboardController)", description = "全平台跨店铺运营大盘、今日 GMV/客单价、待办事项红点、销售趋势与热销排行榜")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 获取今日核心大盘指标概览
     */
    @Operation(summary = "获取今日核心大盘指标概览", description = "聚合全平台今日与累计维度的 GMV、订单量、注册用户数、活跃店铺与商品总数")
    @GetMapping("/overview")
    @RequirePermission("dashboard:view")
    public CommonResult<DashboardOverviewVO> getOverview() {
        DashboardOverviewVO vo = adminDashboardService.getOverview();
        return CommonResult.success(vo);
    }

    /**
     * 获取运营待办事项角标数量
     */
    @Operation(summary = "获取运营待办事项角标数量", description = "统计待审核实名认证、待审批开店申请、待履约发货订单及库存告急预警数量")
    @GetMapping("/todos")
    @RequirePermission("dashboard:view")
    public CommonResult<DashboardTodosVO> getTodos() {
        DashboardTodosVO vo = adminDashboardService.getTodos();
        return CommonResult.success(vo);
    }

    /**
     * 获取销售走势与订单量趋势图表数据
     */
    @Operation(summary = "获取销售走势与订单量趋势图表数据", description = "按自然日粒度统计最近指定天数（默认 7 天）内的销售流水与订单走势")
    @GetMapping("/trend")
    @RequirePermission("dashboard:view")
    public CommonResult<DashboardTrendVO> getTrend(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        DashboardTrendVO vo = adminDashboardService.getTrend(days);
        return CommonResult.success(vo);
    }

    /**
     * 获取热销商品 Top 榜单
     */
    @Operation(summary = "获取热销商品 Top 榜单", description = "按订单交易额与销量降序排名展现全平台爆款热销商品")
    @GetMapping("/top-products")
    @RequirePermission("dashboard:view")
    public CommonResult<List<TopProductVO>> getTopProducts(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        List<TopProductVO> list = adminDashboardService.getTopProducts(limit);
        return CommonResult.success(list);
    }

    /**
     * 获取各品类销售占比
     */
    @Operation(summary = "获取各品类销售占比", description = "统计各大核心一级类目的销售体量与所占全平台总销售额的百分比")
    @GetMapping("/category-ratio")
    @RequirePermission("dashboard:view")
    public CommonResult<List<CategoryRatioVO>> getCategoryRatio() {
        List<CategoryRatioVO> list = adminDashboardService.getCategoryRatio();
        return CommonResult.success(list);
    }
}
