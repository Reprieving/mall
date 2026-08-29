package com.example.baseboot.module.product.sku.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
import com.example.baseboot.module.product.sku.dto.SkuUpdateDTO;
import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.sku.vo.SkuVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品 SKU 业务接口
 */
public interface SkuService extends IService<Sku> {

    /**
     * 单独新增 SKU (自动同步对应 SPU 的价格区间与总库存)
     */
    SkuVO createSku(Long spuId, SkuItemDTO skuDTO);

    /**
     * 修改 SKU 信息 (价格、原价、成本价、图片、规格等，并联动 SPU)
     */
    SkuVO updateSku(Long id, SkuUpdateDTO updateDTO);

    /**
     * 删除 SKU (联动 SPU 重新汇总价格与库存)
     */
    boolean deleteSku(Long id);

    /**
     * 获取 SKU 详情
     */
    SkuVO getSkuById(Long id);

    /**
     * 获取指定 SPU 旗下的所有 SKU 列表
     */
    List<SkuVO> listSkuBySpuId(Long spuId);

    /**
     * 调整 SKU 库存数量 (联动 SPU 汇总)
     */
    boolean updateStock(Long id, Integer stock);

    /**
     * 调整 SKU 价格 (联动 SPU 汇总)
     */
    boolean updatePrice(Long id, BigDecimal price, BigDecimal originalPrice, BigDecimal costPrice);

    /**
     * 启用/禁用 SKU
     */
    boolean updateStatus(Long id, Integer status);
}
