package com.example.baseboot;

import com.example.baseboot.common.utils.IdCardUtils;
import com.example.baseboot.common.utils.PasswordUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BaseBootApplicationTests {

    @Test
    void testPasswordUtils() {
        String rawPassword = "myPassword123";
        String encoded = PasswordUtils.encode(rawPassword);
        Assertions.assertNotNull(encoded);
        Assertions.assertTrue(PasswordUtils.matches(rawPassword, encoded));
        Assertions.assertFalse(PasswordUtils.matches("wrongPassword", encoded));
    }

    @Test
    void testIdCardValidationAndMasking() {
        // 合法身份证测试 (110101199003072391 校验位 1 符合 GB 11643-1999)
        String validIdCard = "110101199003072391";
        Assertions.assertTrue(IdCardUtils.validateIdCard(validIdCard));

        // 校验位错误测试
        String invalidIdCard = "110101199003072390";
        Assertions.assertFalse(IdCardUtils.validateIdCard(invalidIdCard));

        // 长度不足或非法字符
        Assertions.assertFalse(IdCardUtils.validateIdCard("123456789"));
        Assertions.assertFalse(IdCardUtils.validateIdCard(null));

        // 脱敏测试
        Assertions.assertEquals("110101********2391", IdCardUtils.maskIdCard(validIdCard));
        Assertions.assertEquals("张*", IdCardUtils.maskRealName("张伟"));
        Assertions.assertEquals("诸**明", IdCardUtils.maskRealName("诸葛孔明"));
    }
}
