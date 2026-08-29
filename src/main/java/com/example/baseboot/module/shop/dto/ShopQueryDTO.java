package com.example.baseboot.module.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺列表多条件分页查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopQueryDTO {

    /**
     * 店铺名称模糊搜索
     */
    private String name;

    /**
     * 店铺类型: 1-个人店, 2-个体工商户店, 3-企业旗舰店, 4-企业专营店
     */
    private Integer type;

    /**
     * 店铺状态: 0-待审核, 1-正常营业, 2-暂停营业(打烊), 3-审核驳回, 4-违规封禁
     */
    private Integer status;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    @Builder.Default
    private Long pageSize = 10L;
}
