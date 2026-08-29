package com.example.baseboot.module.product.sku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.product.sku.entity.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 SKU Mapper 接口
 */
@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
}
