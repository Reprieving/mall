package com.example.baseboot.module.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 申请开店请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopApplyDTO {

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    private String name;

    /**
     * 店铺 Logo
     */
    @NotBlank(message = "请上传店铺Logo")
    private String logo;

    /**
     * 店铺横幅大图 / 招牌
     */
    private String banner;

    /**
     * 店铺简介
     */
    private String intro;

    /**
     * 店铺公告
     */
    private String notice;

    /**
     * 客服/联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    /**
     * 店铺类型: 1-个人店, 2-个体工商户店, 3-企业旗舰店, 4-企业专营店
     */
    @NotNull(message = "请选择开店类型")
    private Integer type;
}
