package com.example.baseboot.module.user.address.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改用户收货地址请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateDTO {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String name;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    private String phone;

    /**
     * 省份/直辖市
     */
    @NotBlank(message = "省份不能为空")
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 区/县
     */
    @NotBlank(message = "区/县不能为空")
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    /**
     * 邮政编码
     */
    private String postalCode;

    /**
     * 是否默认地址: 0-否, 1-是
     */
    private Integer isDefault;

    /**
     * 地址标签
     */
    private String tag;
}
