package com.example.baseboot.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运营管理员当前请求线程上下文
 */
public class AdminContext {

    private static final ThreadLocal<AdminUserContextInfo> CONTEXT = new ThreadLocal<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminUserContextInfo {
        private Long adminId;
        private String username;
        private String roleCode;
        private List<String> permissions;
    }

    public static void set(AdminUserContextInfo info) {
        CONTEXT.set(info);
    }

    public static AdminUserContextInfo get() {
        return CONTEXT.get();
    }

    public static Long getAdminId() {
        AdminUserContextInfo info = CONTEXT.get();
        return info != null ? info.getAdminId() : null;
    }

    public static String getUsername() {
        AdminUserContextInfo info = CONTEXT.get();
        return info != null ? info.getUsername() : null;
    }

    public static String getRoleCode() {
        AdminUserContextInfo info = CONTEXT.get();
        return info != null ? info.getRoleCode() : null;
    }

    public static List<String> getPermissions() {
        AdminUserContextInfo info = CONTEXT.get();
        return info != null ? info.getPermissions() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
