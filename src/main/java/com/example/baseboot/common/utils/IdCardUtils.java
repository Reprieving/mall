package com.example.baseboot.common.utils;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 身份证号码校验与脱敏工具类 (符合 GB 11643-1999 规范)
 */
public class IdCardUtils {

    /**
     * 18位二代身份证正则
     */
    private static final Pattern ID_CARD_18_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$"
    );

    /**
     * 18位身份证加权因子
     */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 18位身份证校验码对应表
     */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 校验18位身份证号码是否合法
     *
     * @param idCard 身份证号
     * @return 是否合法
     */
    public static boolean validateIdCard(String idCard) {
        if (!StringUtils.hasText(idCard) || idCard.trim().length() != 18) {
            return false;
        }

        idCard = idCard.trim().toUpperCase();

        // 1. 正则初步格式匹配
        if (!ID_CARD_18_PATTERN.matcher(idCard).matches()) {
            return false;
        }

        // 2. 出生日期真实性验证
        try {
            String birthStr = idCard.substring(6, 14);
            LocalDate birthDate = LocalDate.parse(birthStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate now = LocalDate.now();
            if (birthDate.isAfter(now) || birthDate.isBefore(LocalDate.of(1900, 1, 1))) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // 3. 校验码加权模 11 计算与比对
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        int mod = sum % 11;
        char expectedCheckCode = CHECK_CODES[mod];
        char actualCheckCode = idCard.charAt(17);

        return expectedCheckCode == actualCheckCode;
    }

    /**
     * 身份证号码脱敏 (保留前6位和后4位，中间脱敏，如: 110101********1234)
     *
     * @param idCard 身份证号
     * @return 脱敏后的字符串
     */
    public static String maskIdCard(String idCard) {
        if (!StringUtils.hasText(idCard)) {
            return "";
        }
        idCard = idCard.trim();
        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        return idCard.replaceAll("(\\d{3})\\d+(\\d{4})", "$1****$2");
    }

    /**
     * 真实姓名脱敏 (如: 张*、李*华、欧阳**等)
     *
     * @param realName 真实姓名
     * @return 脱敏后的姓名
     */
    public static String maskRealName(String realName) {
        if (!StringUtils.hasText(realName)) {
            return "";
        }
        realName = realName.trim();
        int len = realName.length();
        if (len <= 1) {
            return realName;
        } else if (len == 2) {
            return realName.charAt(0) + "*";
        } else {
            return realName.charAt(0) + "*".repeat(len - 2) + realName.charAt(len - 1);
        }
    }
}
