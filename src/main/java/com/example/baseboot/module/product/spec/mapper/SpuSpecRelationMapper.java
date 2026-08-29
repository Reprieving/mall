package com.example.baseboot.module.product.spec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.product.spec.entity.SpuSpecRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * SPU 规格选用关联 Mapper 接口
 */
@Mapper
public interface SpuSpecRelationMapper extends BaseMapper<SpuSpecRelation> {
}
