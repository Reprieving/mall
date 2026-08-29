package com.example.baseboot.module.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改店铺信息请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopUpdateDTO {

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
}
