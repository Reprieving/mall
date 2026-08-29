package com.example.baseboot.module.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;

/**
 * 店铺 Mapper 接口
 */
@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
}
