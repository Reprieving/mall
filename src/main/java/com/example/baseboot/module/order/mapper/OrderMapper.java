package com.example.baseboot.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
