package com.example.baseboot;

import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.vo.ShopDetailVO;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class ShopModuleTests {

    @Test
    void testShopVOConversion() {
        Shop shop = Shop.builder()
                .id(1L)
                .userId(100L)
                .certId(10L)
                .name("极光数码官方旗舰店")
                .logo("https://example.com/logo.png")
                .banner("https://example.com/banner.png")
                .intro("官方自营旗舰店")
                .notice("全场新品现货")
                .phone("400-888-9999")
                .type(3) // 3-企业旗舰店
                .status(1) // 1-正常营业
                .score(new BigDecimal("4.98"))
                .createTime(LocalDateTime.now())
                .build();

        ShopVO vo = ShopVO.fromEntity(shop);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals("极光数码官方旗舰店", vo.getName());
        Assertions.assertEquals("企业旗舰店", vo.getTypeName());
        Assertions.assertEquals("正常营业", vo.getStatusName());
        Assertions.assertEquals(new BigDecimal("4.98"), vo.getScore());
    }

    @Test
    void testShopDetailVOAssembly() {
        Shop shop = Shop.builder()
                .id(1L)
                .userId(100L)
                .certId(10L)
                .name("极光数码官方旗舰店")
                .type(3)
                .status(1)
                .score(new BigDecimal("5.00"))
                .build();

        UserCertification cert = UserCertification.builder()
                .id(10L)
                .userId(100L)
                .certType(3)
                .realName("张伟")
                .companyName("北京极光科技有限公司")
                .status(1)
                .build();

        ShopDetailVO detailVO = ShopDetailVO.builder()
                .shopInfo(ShopVO.fromEntity(shop))
                .certInfo(UserCertVO.fromEntity(cert))
                .build();

        Assertions.assertNotNull(detailVO.getShopInfo());
        Assertions.assertNotNull(detailVO.getCertInfo());
        Assertions.assertEquals("极光数码官方旗舰店", detailVO.getShopInfo().getName());
        Assertions.assertEquals("北京极光科技有限公司", detailVO.getCertInfo().getCompanyName());
    }
}
