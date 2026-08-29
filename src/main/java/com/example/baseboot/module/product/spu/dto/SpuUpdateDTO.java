package com.example.baseboot.module.product.spu.dto;

import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新 SPU 商品请求参数
 */
@Data
public class SpuUpdateDTO {

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
     * 商品货号/编码
     */
    private String spuCode;

    /**
     * 所属分类ID
     */
    @NotNull(message = "所属分类ID不能为空")
    private Long categoryId;

    /**
     * 所属品牌ID
     */
    private Long brandId;

    /**
     * 商品副标题/促销卖点
     */
    private String title;

    /**
     * 商品图文详情
     */
    private String description;

    /**
     * 商品主图 URL
     */
    private String mainPic;

    /**
     * 轮播相册列表 (JSON 字符串或逗号分隔)
     */
    private String sliderPics;

    /**
     * 规格类型: 0-单规格, 1-多规格
     */
    private Integer specType;

    /**
     * 计量单位 (如: 件, 台)
     */
    private String unit;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 上架状态: 0-下架, 1-上架
     */
    private Integer status;

    /**
     * 规格定义项列表
     */
    @Valid
    private List<SpuSpecItemDTO> specList;

    /**
     * SKU 列表
     */
    @NotEmpty(message = "商品SKU列表不能为空")
    @Valid
    private List<SkuItemDTO> skuList;
}
