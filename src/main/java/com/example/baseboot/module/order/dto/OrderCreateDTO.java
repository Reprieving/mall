package com.example.baseboot.module.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建订单请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    /**
     * 省份/直辖市
     */
    private String receiverProvince;

    /**
     * 城市
     */
    private String receiverCity;

    /**
     * 区/县
     */
    private String receiverDistrict;

    /**
     * 详细收货地址
     */
    @NotBlank(message = "详细收货地址不能为空")
    private String receiverDetailAddress;

    /**
     * 买家留言/订单备注
     */
    private String note;

    /**
     * 支付方式: 0-未支付, 1-支付宝, 2-微信支付, 3-银联, 4-余额支付
     */
    private Integer payType;

    /**
     * 购买的商品 SKU 列表
     */
    @NotEmpty(message = "购买商品条目不能为空")
    @Valid
    private List<OrderItemParamDTO> items;
}
