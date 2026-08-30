package com.example.baseboot.module.dashboard.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 热销商品排行视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long spuId;
    private String spuName;
    private String spuPic;
    private Integer salesCount;
    private BigDecimal salesAmount;
}
