package com.example.baseboot.module.product.spu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 SPU 实体类 (对应表 pms_spu)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_spu")
public class Spu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SPU ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品编码/货号
     */
    private String spuCode;

    /**
     * 所属分类ID
     */
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
     * 商品图文详情 (富文本/Markdown)
     */
    private String description;

    /**
     * 商品主图 URL
     */
    private String mainPic;

    /**
     * 轮播相册图 (JSON 数组或逗号分隔)
     */
    private String sliderPics;

    /**
     * 规格类型: 0-单规格, 1-多规格
     */
    private Integer specType;

    /**
     * 最低售价/展示价
     */
    private BigDecimal minPrice;

    /**
     * 最高售价
     */
    private BigDecimal maxPrice;

    /**
     * 商品总库存
     */
    private Integer totalStock;

    /**
     * 计量单位 (如: 件, 台, 套)
     */
    private String unit;

    /**
     * 上架状态: 0-下架, 1-上架
     */
    private Integer status;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
