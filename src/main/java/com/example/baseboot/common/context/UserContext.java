package com.example.baseboot.common.context;

/**
 * 当前登录用户信息上下文 (基于 ThreadLocal)
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL_HOLDER = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void setUserEmail(String email) {
        USER_EMAIL_HOLDER.set(email);
    }

    public static String getUserEmail() {
        return USER_EMAIL_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
        USER_EMAIL_HOLDER.remove();
    }
}
