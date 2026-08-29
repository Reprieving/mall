package com.example.baseboot.module.admin.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.common.utils.JwtUtils;
import com.example.baseboot.common.utils.PasswordUtils;
import com.example.baseboot.module.admin.auth.dto.AdminLoginDTO;
import com.example.baseboot.module.admin.auth.service.AdminAuthService;
import com.example.baseboot.module.admin.auth.vo.AdminInfoVO;
import com.example.baseboot.module.admin.auth.vo.AdminLoginVO;
import com.example.baseboot.module.admin.system.entity.SysAdminUser;
import com.example.baseboot.module.admin.system.entity.SysRole;
import com.example.baseboot.module.admin.system.mapper.SysAdminUserMapper;
import com.example.baseboot.module.admin.system.mapper.SysRoleMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 运营管理员认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SysAdminUserMapper adminUserMapper;
    private final SysRoleMapper roleMapper;
    private final JwtUtils jwtUtils;
    private final RedissonClient redissonClient;

    private static final String ADMIN_TOKEN_KEY_PREFIX = "auth:admin:token:";
    private static final String ADMIN_PERMISSIONS_KEY_PREFIX = "auth:admin:permissions:";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AdminLoginVO login(AdminLoginDTO loginDTO) {
        if (loginDTO == null || !StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "账号和密码不能为空");
        }

        SysAdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, loginDTO.getUsername().trim()));

        if (admin == null || !PasswordUtils.matches(loginDTO.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.ADMIN_PASSWORD_ERROR);
        }

        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException(ResultCode.ADMIN_DISABLED);
        }

        SysRole role = roleMapper.selectById(admin.getRoleId());
        String roleName = role != null ? role.getName() : "普通角色";
        String roleCode = role != null ? role.getCode() : "ADMIN";

        List<String> permissions = parsePermissions(role != null ? role.getPermissions() : null);

        String token = jwtUtils.generateAdminToken(admin.getId(), admin.getUsername(), roleCode);

        // 写入 Redisson 缓存
        Duration ttl = Duration.ofMillis(jwtUtils.getExpiration());
        RBucket<String> tokenBucket = redissonClient.getBucket(ADMIN_TOKEN_KEY_PREFIX + admin.getId());
        tokenBucket.set(token, ttl);

        RBucket<String> permBucket = redissonClient.getBucket(ADMIN_PERMISSIONS_KEY_PREFIX + admin.getId());
        try {
            permBucket.set(OBJECT_MAPPER.writeValueAsString(permissions), ttl);
        } catch (Exception ignored) {
        }

        return AdminLoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatar(admin.getAvatar())
                .roleName(roleName)
                .roleCode(roleCode)
                .token(token)
                .tokenPrefix(jwtUtils.getTokenPrefix())
                .expiresIn(jwtUtils.getExpiration())
                .permissions(permissions)
                .build();
    }

    @Override
    public AdminInfoVO getInfo(Long adminId) {
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysAdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_EXIST);
        }

        SysRole role = roleMapper.selectById(admin.getRoleId());
        String roleName = role != null ? role.getName() : "";
        String roleCode = role != null ? role.getCode() : "";
        List<String> permissions = parsePermissions(role != null ? role.getPermissions() : null);

        return AdminInfoVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .avatar(admin.getAvatar())
                .email(admin.getEmail())
                .phone(admin.getPhone())
                .roleId(admin.getRoleId())
                .roleName(roleName)
                .roleCode(roleCode)
                .permissions(permissions)
                .build();
    }

    @Override
    public void logout(Long adminId) {
        if (adminId != null) {
            redissonClient.getBucket(ADMIN_TOKEN_KEY_PREFIX + adminId).delete();
            redissonClient.getBucket(ADMIN_PERMISSIONS_KEY_PREFIX + adminId).delete();
        }
    }

    private List<String> parsePermissions(String permJson) {
        if (!StringUtils.hasText(permJson)) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(permJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.singletonList(permJson.trim());
        }
    }
}
