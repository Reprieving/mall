package com.example.baseboot.module.product.spec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.module.product.spec.dto.SkuSpecValueItemDTO;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;

import java.util.List;
import java.util.Map;

/**
 * SKU 规格绑定关系业务接口
 */
public interface SkuSpecValueService extends IService<SkuSpecValue> {

    /**
     * 批量保存/重置 SKU 的规格绑定关系
     */
    void saveSkuSpecValues(Long spuId, Long skuId, List<SkuSpecValueItemDTO> specValueItems);

    /**
     * 根据 SKU ID 查询绑定的规格列表
     */
    List<SkuSpecValueVO> listBySkuId(Long skuId);

    /**
     * 批量查询多个 SKU 绑定的规格列表 (Map<skuId, List<SkuSpecValueVO>>)
     */
    Map<Long, List<SkuSpecValueVO>> mapBySkuIds(List<Long> skuIds);

    /**
     * 根据 SPU ID 删除其旗下所有规格绑定记录
     */
    void deleteBySpuId(Long spuId);

    /**
     * 根据 SKU ID 删除规格绑定记录
     */
    void deleteBySkuId(Long skuId);
}
