package com.example.baseboot.module.product.spu.dto;

import lombok.Data;

/**
 * SPU 商品分页查询参数
 */
@Data
public class SpuQueryDTO {

    /**
     * 商品名称/副标题搜索关键字
     */
    private String keyword;

    /**
     * 商品货号/编码
     */
    private String spuCode;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * 所属品牌ID
     */
    private Long brandId;

    /**
     * 上架状态: 0-下架, 1-上架
     */
    private Integer status;

    /**
     * 当前页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
