package com.example.baseboot.interceptor;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.context.AdminContext;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.common.utils.JwtUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 统一登录认证、管理员角色与 RBAC 权限拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedissonClient redissonClient;

    private static final String USER_TOKEN_KEY_PREFIX = "auth:token:";
    private static final String ADMIN_TOKEN_KEY_PREFIX = "auth:admin:token:";
    private static final String ADMIN_PERMISSIONS_KEY_PREFIX = "auth:admin:permissions:";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是映射到方法则直接放行 (如预检请求 OPTIONS 或静态资源)
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Method method = handlerMethod.getMethod();
        Class<?> clazz = handlerMethod.getBeanType();

        // 检查是否有 @PassToken 注解，有则直接放行
        if (method.isAnnotationPresent(PassToken.class) || clazz.isAnnotationPresent(PassToken.class)) {
            return true;
        }

        String uri = request.getRequestURI();
        String authHeader = request.getHeader(jwtUtils.getHeader());

        // 1. 拦截管理端接口 (/api/admin/** 或带有 @RequirePermission)
        boolean isAdminEndpoint = uri.startsWith("/api/admin") || method.isAnnotationPresent(RequirePermission.class) || clazz.isAnnotationPresent(RequirePermission.class);

        if (isAdminEndpoint) {
            if (!StringUtils.hasText(authHeader)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "请先以管理员身份登录（缺少 Authorization 请求头）");
            }

            if (!jwtUtils.validateToken(authHeader)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "管理员 Token 无效或已过期");
            }

            String userType = jwtUtils.getUserTypeFromToken(authHeader);
            if (!"ADMIN".equalsIgnoreCase(userType)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "非管理员账号，无权访问运营端接口");
            }

            Long adminId = jwtUtils.getAdminIdFromToken(authHeader);
            String username = jwtUtils.getUsernameFromToken(authHeader);
            String roleCode = jwtUtils.getRoleCodeFromToken(authHeader);

            if (adminId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "无效的管理员身份凭证");
            }

            // 校验 Redisson 会话
            RBucket<String> sessionBucket = redissonClient.getBucket(ADMIN_TOKEN_KEY_PREFIX + adminId);
            String cachedToken = sessionBucket.get();
            if (cachedToken == null) {
                log.warn("管理员 [{}] 会话已过期或已被下线", adminId);
                throw new BusinessException(ResultCode.UNAUTHORIZED, "管理员登录已失效，请重新登录");
            }

            // 读取权限列表
            RBucket<String> permBucket = redissonClient.getBucket(ADMIN_PERMISSIONS_KEY_PREFIX + adminId);
            List<String> permissions = Collections.emptyList();
            String permJson = permBucket.get();
            if (StringUtils.hasText(permJson)) {
                try {
                    permissions = OBJECT_MAPPER.readValue(permJson, new TypeReference<List<String>>() {});
                } catch (Exception ignored) {
                }
            }

            // 权限细粒度校验
            RequirePermission requirePerm = method.getAnnotation(RequirePermission.class);
            if (requirePerm == null) {
                requirePerm = clazz.getAnnotation(RequirePermission.class);
            }

            if (requirePerm != null && StringUtils.hasText(requirePerm.value())) {
                String targetPerm = requirePerm.value().trim();
                boolean hasPermission = permissions.contains("*:*:*") || permissions.contains(targetPerm);
                if (!hasPermission) {
                    throw new BusinessException(ResultCode.FORBIDDEN, "您没有操作权限: " + targetPerm);
                }
            }

            AdminContext.set(AdminContext.AdminUserContextInfo.builder()
                    .adminId(adminId)
                    .username(username)
                    .roleCode(roleCode)
                    .permissions(permissions)
                    .build());

            return true;
        }

        // 2. 普通买家/商家端或通用业务接口拦截 (@LoginRequired / /api/user/**)
        boolean loginRequired = method.isAnnotationPresent(LoginRequired.class) 
                || clazz.isAnnotationPresent(LoginRequired.class)
                || uri.startsWith("/api/user");

        if (loginRequired) {
            if (!StringUtils.hasText(authHeader)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录（缺少 Authorization 请求头）");
            }

            if (!jwtUtils.validateToken(authHeader)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "Token 无效或已过期");
            }

            String userType = jwtUtils.getUserTypeFromToken(authHeader);

            if ("ADMIN".equalsIgnoreCase(userType)) {
                // 如果携带的是有效的管理员 Token，放行并注入管理员上下文
                Long adminId = jwtUtils.getAdminIdFromToken(authHeader);
                String username = jwtUtils.getUsernameFromToken(authHeader);
                String roleCode = jwtUtils.getRoleCodeFromToken(authHeader);

                if (adminId == null) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED, "无效的管理员身份凭证");
                }

                RBucket<String> sessionBucket = redissonClient.getBucket(ADMIN_TOKEN_KEY_PREFIX + adminId);
                String cachedToken = sessionBucket.get();
                if (cachedToken == null) {
                    log.warn("管理员 [{}] 在 Redisson 中的会话已失效或已退出", adminId);
                    throw new BusinessException(ResultCode.UNAUTHORIZED, "管理员登录已失效，请重新登录");
                }

                AdminContext.set(AdminContext.AdminUserContextInfo.builder()
                        .adminId(adminId)
                        .username(username)
                        .roleCode(roleCode)
                        .build());
                // 同步设置 UserContext 供通用方法读取
                UserContext.setUserId(adminId);
                UserContext.setUserEmail(username);
            } else {
                // 普通用户 Token 处理
                Long userId = jwtUtils.getUserIdFromToken(authHeader);
                String email = jwtUtils.getEmailFromToken(authHeader);

                if (userId == null) {
                    throw new BusinessException(ResultCode.UNAUTHORIZED, "无效的用户身份凭证");
                }

                RBucket<String> sessionBucket = redissonClient.getBucket(USER_TOKEN_KEY_PREFIX + userId);
                String cachedToken = sessionBucket.get();
                if (cachedToken == null) {
                    log.warn("用户 [{}] 在 Redisson 中的会话已失效或已退出", userId);
                    throw new BusinessException(ResultCode.UNAUTHORIZED, "登录已失效，请重新登录");
                }

                UserContext.setUserId(userId);
                UserContext.setUserEmail(email);
            }
        } else if (StringUtils.hasText(authHeader) && jwtUtils.validateToken(authHeader)) {
            String userType = jwtUtils.getUserTypeFromToken(authHeader);
            if ("ADMIN".equalsIgnoreCase(userType)) {
                Long adminId = jwtUtils.getAdminIdFromToken(authHeader);
                String username = jwtUtils.getUsernameFromToken(authHeader);
                AdminContext.set(AdminContext.AdminUserContextInfo.builder()
                        .adminId(adminId)
                        .username(username)
                        .build());
            } else {
                Long userId = jwtUtils.getUserIdFromToken(authHeader);
                String email = jwtUtils.getEmailFromToken(authHeader);
                UserContext.setUserId(userId);
                UserContext.setUserEmail(email);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        AdminContext.clear();
    }
}

