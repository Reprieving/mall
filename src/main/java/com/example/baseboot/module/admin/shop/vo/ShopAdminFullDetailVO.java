package com.example.baseboot.module.admin.shop.vo;

import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运营端店铺全量全景详情视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopAdminFullDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺主信息
     */
    private ShopVO shop;

    /**
     * 主体认证资质
     */
    private UserCertVO certification;

    /**
     * 店主个人账号资料
     */
    private UserVO owner;

    /**
     * 店铺名下商品数
     */
    private Long productCount;
}
