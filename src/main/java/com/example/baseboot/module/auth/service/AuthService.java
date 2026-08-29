package com.example.baseboot.module.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.common.utils.JwtUtils;
import com.example.baseboot.common.utils.PasswordUtils;
import com.example.baseboot.module.auth.dto.*;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.entity.SysUser;
import com.example.baseboot.module.user.profile.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Random;

/**
 * 认证与授权业务服务 (集成 MyBatis-Plus 与 Redisson)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String CODE_KEY_PREFIX = "auth:code:";
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final RedissonClient redissonClient;

    /**
     * 发送邮箱验证码 (保存到 Redisson，有效时间 5 分钟)
     */
    public String sendEmailCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "邮箱不能为空");
        }

        // 生成 6 位随机数字验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 存入 Redisson 缓存，有效期 5 分钟
        RBucket<String> bucket = redissonClient.getBucket(CODE_KEY_PREFIX + email);
        bucket.set(code, Duration.ofMinutes(5));

        log.info("【系统通知】已向邮箱 [{}] 发送验证码: [{}] (有效期5分钟)", email, code);
        return code;
    }

    /**
     * 邮箱 + 密码登录
     */
    public LoginResponse loginByPassword(EmailLoginRequest request) {
        SysUser user = userService.getByEmail(request.getEmail());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 密码比对
        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        return createLoginSession(user);
    }

    /**
     * 邮箱 + 验证码登录 (若用户不存在则自动注册并登录)
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse loginByCode(EmailCodeLoginRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        // 校验 Redisson 中的验证码
        RBucket<String> bucket = redissonClient.getBucket(CODE_KEY_PREFIX + email);
        String cachedCode = bucket.get();

        if (cachedCode == null || !cachedCode.equalsIgnoreCase(code)) {
            throw new BusinessException(ResultCode.VERIFY_CODE_ERROR);
        }

        // 验证码使用后立即清除
        bucket.delete();

        // 查询用户是否存在，不存在则基于 MyBatis-Plus 自动保存新用户
        SysUser user = userService.getByEmail(email);
        if (user == null) {
            String defaultUsername = email.substring(0, email.indexOf("@"));
            user = SysUser.builder()
                    .email(email)
                    .username(defaultUsername)
                    .password(PasswordUtils.encode("123456"))
                    .nickname(defaultUsername)
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + defaultUsername)
                    .status(1)
                    .build();
            userService.save(user);
            log.info("新邮箱用户免密登录自动完成注册: id={}, email={}", user.getId(), email);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        return createLoginSession(user);
    }

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterRequest request) {
        // 检查邮箱唯一性 (使用 MyBatis-Plus LambdaQueryWrapper)
        long count = userService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, request.getEmail()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }

        SysUser newUser = SysUser.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(PasswordUtils.encode(request.getPassword()))
                .nickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername())
                .status(1)
                .build();

        userService.save(newUser);
        return UserVO.fromEntity(newUser);
    }

    /**
     * 退出登录 (清除 Redisson 会话缓存)
     */
    public void logout(Long userId) {
        if (userId != null) {
            RBucket<String> sessionBucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + userId);
            sessionBucket.delete();
            log.info("用户 [{}] 成功退出登录", userId);
        }
    }

    /**
     * 创建登录会话并生成 JWT
     */
    private LoginResponse createLoginSession(SysUser user) {
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getUsername());

        // 缓存当前用户的 Token 至 Redisson，支持单点登录或服务端主动注销
        RBucket<String> sessionBucket = redissonClient.getBucket(TOKEN_KEY_PREFIX + user.getId());
        sessionBucket.set(token, Duration.ofMillis(jwtUtils.getExpiration()));

        return LoginResponse.builder()
                .token(token)
                .tokenType(jwtUtils.getTokenPrefix().trim())
                .expiresIn(jwtUtils.getExpiration() / 1000)
                .userInfo(UserVO.fromEntity(user))
                .build();
    }
}
