package com.example.baseboot;

import com.example.baseboot.module.user.address.entity.UserAddress;
import com.example.baseboot.module.user.address.vo.AddressVO;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class UserAddressAndCertTests {

    @Test
    void testAddressFullAddressFormatting() {
        UserAddress address = UserAddress.builder()
                .id(1L)
                .userId(100L)
                .name("张伟")
                .phone("13800138000")
                .province("北京市")
                .city("北京市")
                .district("海淀区")
                .detailAddress("中关村南大街1号院8号楼")
                .isDefault(1)
                .tag("公司")
                .createTime(LocalDateTime.now())
                .build();

        AddressVO vo = AddressVO.fromEntity(address);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals("北京市北京市海淀区中关村南大街1号院8号楼", vo.getFullAddress());
        Assertions.assertEquals(1, vo.getIsDefault());
        Assertions.assertEquals("公司", vo.getTag());
    }

    @Test
    void testPersonalCertVOConversion() {
        UserCertification cert = UserCertification.builder()
                .id(1L)
                .userId(101L)
                .certType(1)
                .realName("李四")
                .idCard("110101199001011234")
                .idCardFrontPic("https://example.com/front.jpg")
                .idCardBackPic("https://example.com/back.jpg")
                .status(1)
                .createTime(LocalDateTime.now())
                .build();

        UserCertVO vo = UserCertVO.fromEntity(cert);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals("个人实名认证", vo.getCertTypeName());
        Assertions.assertEquals("审核通过", vo.getStatusName());
        Assertions.assertEquals("李四", vo.getRealName());
    }

    @Test
    void testEnterpriseCertVOConversion() {
        UserCertification cert = UserCertification.builder()
                .id(2L)
                .userId(102L)
                .certType(3)
                .realName("王五")
                .idCard("110101199002022345")
                .idCardFrontPic("https://example.com/front.jpg")
                .idCardBackPic("https://example.com/back.jpg")
                .companyName("北京极光科技有限公司")
                .businessLicenseNo("91110108MA01ABCD12")
                .businessLicensePic("https://example.com/license.jpg")
                .companyAddress("北京市海淀区中关村南大街1号")
                .status(0)
                .createTime(LocalDateTime.now())
                .build();

        UserCertVO vo = UserCertVO.fromEntity(cert);
        Assertions.assertNotNull(vo);
        Assertions.assertEquals("企业认证", vo.getCertTypeName());
        Assertions.assertEquals("待审核", vo.getStatusName());
        Assertions.assertEquals("北京极光科技有限公司", vo.getCompanyName());
    }
}
