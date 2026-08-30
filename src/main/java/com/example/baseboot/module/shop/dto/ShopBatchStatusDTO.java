package com.example.baseboot.module.shop.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运营端批量调整店铺状态参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopBatchStatusDTO {

    /**
     * 店铺ID列表
     */
    @NotEmpty(message = "店铺ID列表不能为空")
    private List<Long> ids;

    /**
     * 目标状态: 1-正常营业, 2-暂停营业/打烊, 4-违规封禁
     */
    @NotNull(message = "目标状态不能为空")
    private Integer status;
}
