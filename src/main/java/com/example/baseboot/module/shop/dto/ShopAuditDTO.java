package com.example.baseboot.module.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核开店申请请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopAuditDTO {

    /**
     * 店铺ID
     */
    @NotNull(message = "店铺ID不能为空")
    private Long id;

    /**
     * 审核结果: 1-审核通过, 3-审核驳回
     */
    @NotNull(message = "审核状态不能为空 (1-通过, 3-驳回)")
    private Integer status;

    /**
     * 驳回原因 (驳回时必填)
     */
    private String rejectReason;
}
