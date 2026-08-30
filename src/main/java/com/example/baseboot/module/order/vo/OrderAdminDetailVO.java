package com.example.baseboot.module.order.vo;

import com.example.baseboot.module.order.vo.OrderItemVO;
import com.example.baseboot.module.order.vo.OrderVO;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 运营端全量订单详情视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAdminDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单主体基础信息 (含实付金额、运营插旗、内部备注)
     */
    private OrderVO orderInfo;

    /**
     * 订单商品明细与下单快照
     */
    private List<OrderItemVO> items;

    /**
     * 下单买家资料
     */
    private UserVO buyer;

    /**
     * 所属店铺资料
     */
    private ShopVO shop;

    /**
     * 订单全流程流转时间轴日志
     */
    private List<OrderLogVO> timeline;
}
