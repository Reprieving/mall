package com.example.baseboot.module.product.spec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import org.apache.ibatis.annotations.Mapper;

/**
 * SKU 规格绑定关系 Mapper 接口
 */
@Mapper
public interface SkuSpecValueMapper extends BaseMapper<SkuSpecValue> {
}
