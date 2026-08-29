package com.example.baseboot.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细项 Mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
