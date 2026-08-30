package com.example.baseboot.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分类销售占比视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRatioVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private String categoryName;
    private BigDecimal salesAmount;
    private BigDecimal ratio;
}
