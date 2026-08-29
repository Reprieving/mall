package com.example.baseboot.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密与校验工具类 (基于 BCrypt 强哈希算法)
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 对明文密码进行 BCrypt 加密
     *
     * @param rawPassword 明文密码
     * @return 加密后的密文哈希
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码与密文是否匹配
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 密文哈希
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
