package com.example.baseboot.module.auth.controller;

import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.auth.dto.*;
import com.example.baseboot.module.auth.service.AuthService;
import com.example.baseboot.module.user.profile.dto.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证授权控制器
 */
@Tag(name = "01. 用户认证授权 (AuthController)", description = "买家端用户注册、发送邮箱验证码、密码登录、免密登录与退出登录")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送邮箱验证码
     */
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送 6 位数字验证码，有效期 5 分钟，带有防刷机制")
    @PassToken
    @PostMapping("/send-code")
    public CommonResult<Map<String, String>> sendCode(@Valid @RequestBody SendCodeRequest request) {
        String code = authService.sendEmailCode(request.getEmail());
        Map<String, String> result = new HashMap<>();
        result.put("email", request.getEmail());
        // 开发测试环境下返回验证码便于直接调试
        result.put("code", code);
        return CommonResult.success(result, "验证码发送成功（5分钟内有效）");
    }

    /**
     * 邮箱 + 密码登录
     */
    @Operation(summary = "邮箱密码登录", description = "普通用户通过邮箱与密码进行身份认证，成功后签发 JWT Token")
    @PassToken
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(@Valid @RequestBody EmailLoginRequest request) {
        LoginResponse response = authService.loginByPassword(request);
        return CommonResult.success(response, "登录成功");
    }

    /**
     * 邮箱 + 验证码免密登录
     */
    @Operation(summary = "邮箱验证码快捷登录", description = "免密快捷登录，通过邮箱验证码直接认证登录")
    @PassToken
    @PostMapping("/login-code")
    public CommonResult<LoginResponse> loginByCode(@Valid @RequestBody EmailCodeLoginRequest request) {
        LoginResponse response = authService.loginByCode(request);
        return CommonResult.success(response, "登录成功");
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户邮箱注册", description = "使用邮箱、密码与验证码完成新买家账号注册并自动登录")
    @PassToken
    @PostMapping("/register")
    public CommonResult<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO userVO = authService.register(request);
        return CommonResult.success(userVO, "注册成功");
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "销毁当前登录会话并清理 Redis 中的 Token 缓存")
    @PostMapping("/logout")
    public CommonResult<Void> logout() {
        Long userId = UserContext.getUserId();
        authService.logout(userId);
        return CommonResult.success(null, "退出登录成功");
    }
}
