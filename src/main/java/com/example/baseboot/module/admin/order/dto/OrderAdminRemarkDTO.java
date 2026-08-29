package com.example.baseboot.module.admin.order.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营端订单插旗与备注请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAdminRemarkDTO {

    /**
     * 运营插旗标色: 0-无, 1-红旗, 2-黄旗, 3-绿旗, 4-蓝旗, 5-紫旗
     */
    @Min(value = 0, message = "旗标值范围为 0-5")
    @Max(value = 5, message = "旗标值范围为 0-5")
    private Integer adminFlag;

    /**
     * 运营内部备注 / 备忘录
     */
    private String adminRemark;
}
