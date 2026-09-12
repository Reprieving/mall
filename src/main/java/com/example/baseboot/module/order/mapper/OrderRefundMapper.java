package com.example.baseboot.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.order.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单退款申请 Mapper 接口
 */
@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {
}
