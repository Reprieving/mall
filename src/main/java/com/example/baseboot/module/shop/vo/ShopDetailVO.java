package com.example.baseboot.module.shop.vo;

import com.example.baseboot.module.user.cert.vo.UserCertVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 店铺详情视图对象 (包含店铺基础信息与主体认证资质)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺主信息
     */
    private ShopVO shopInfo;

    /**
     * 主体认证资质详情
     */
    private UserCertVO certInfo;
}
