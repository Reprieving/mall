package com.example.baseboot.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 签发与解析工具类
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret:YmFzZS1ib290LXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1hdXRoZW50aWNhdGlvbi0yMDI2}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Value("${jwt.header:Authorization}")
    private String header;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 JWT Token (普通用户)
     *
     * @param userId   用户ID
     * @param email    邮箱
     * @param username 用户名
     * @return token 字符串
     */
    public String generateToken(Long userId, String email, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", "USER");
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("username", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成管理员专用 JWT Token
     *
     * @param adminId  管理员ID
     * @param username 管理员账号
     * @param roleCode 角色标识
     * @return token 字符串
     */
    public String generateAdminToken(Long adminId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", "ADMIN");
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject("ADMIN_" + adminId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token 中的 Claims
     *
     * @param token JWT Token
     * @return Claims 负载
     */
    public Claims parseToken(String token) {
        if (token != null && token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length());
        }
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT Token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Token 中获取用户类型 (USER / ADMIN)
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("userType") : null;
    }

    /**
     * 从 Token 中获取管理员ID
     */
    public Long getAdminIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object adminId = claims.get("adminId");
            if (adminId instanceof Number number) {
                return number.longValue();
            } else if (adminId instanceof String str) {
                return Long.parseLong(str);
            }
        }
        return null;
    }

    /**
     * 从 Token 中获取角色代码
     */
    public String getRoleCodeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("roleCode") : null;
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Number number) {
                return number.longValue();
            } else if (userId instanceof String str) {
                return Long.parseLong(str);
            }
        }
        return null;
    }

    /**
     * 从 Token 中获取用户邮箱
     */
    public String getEmailFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("email") : null;
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return !claims.getExpiration().before(new Date());
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public String getHeader() {
        return header;
    }

    public long getExpiration() {
        return expiration;
    }
}
