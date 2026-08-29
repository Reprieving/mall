package com.example.baseboot.module.product.spec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品规格项 Mapper 接口
 */
@Mapper
public interface SpecKeyMapper extends BaseMapper<SpecKey> {
}
