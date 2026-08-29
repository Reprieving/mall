package com.example.baseboot.module.product.spu.vo;

import com.example.baseboot.module.product.brand.vo.BrandVO;
import com.example.baseboot.module.product.category.vo.CategoryVO;
import com.example.baseboot.module.product.sku.vo.SkuVO;
import com.example.baseboot.module.shop.vo.ShopVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SPU 完整详情视图对象 (包含基础信息、所属分类、所属品牌、所属店铺、多规格定义项及关联的所有 SKU)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SPU 基础信息
     */
    private SpuVO spuInfo;

    /**
     * 所属店铺详情
     */
    private ShopVO shop;

    /**
     * 所属分类详情
     */
    private CategoryVO category;

    /**
     * 所属品牌详情
     */
    private BrandVO brand;

    /**
     * 规格定义项列表
     */
    private List<SpuSpecVO> specList;

    /**
     * 旗下 SKU 列表
     */
    private List<SkuVO> skuList;
}
