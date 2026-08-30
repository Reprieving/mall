package com.example.baseboot.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售走势与订单量趋势图表数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期刻度列表 (如: ["2026-08-23", "2026-08-24", ...])
     */
    private List<String> dates;

    /**
     * 对应日期的销售额 (GMV) 列表
     */
    private List<BigDecimal> gmvList;

    /**
     * 对应日期的下单笔数列表
     */
    private List<Integer> orderCountList;
}
