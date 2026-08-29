package com.example.baseboot.module.admin.user.vo;

import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.address.vo.AddressVO;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 运营端买家用户全景档案详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户基础信息
     */
    private UserVO profile;

    /**
     * 累计订单数
     */
    private Integer orderCount;

    /**
     * 累计消费总金额
     */
    private BigDecimal totalSpent;

    /**
     * 实名认证信息
     */
    private UserCertVO certification;

    /**
     * 关联店铺信息 (若已开店)
     */
    private ShopVO shop;

    /**
     * 收货地址列表
     */
    private List<AddressVO> addresses;
}
