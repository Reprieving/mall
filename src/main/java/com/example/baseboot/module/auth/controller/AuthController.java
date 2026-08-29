package com.example.baseboot.module.auth.controller;

import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.auth.dto.*;
import com.example.baseboot.module.auth.service.AuthService;
import com.example.baseboot.module.user.profile.dto.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证授权控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送邮箱验证码
     */
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
    @PassToken
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(@Valid @RequestBody EmailLoginRequest request) {
        LoginResponse response = authService.loginByPassword(request);
        return CommonResult.success(response, "登录成功");
    }

    /**
     * 邮箱 + 验证码免密登录
     */
    @PassToken
    @PostMapping("/login-code")
    public CommonResult<LoginResponse> loginByCode(@Valid @RequestBody EmailCodeLoginRequest request) {
        LoginResponse response = authService.loginByCode(request);
        return CommonResult.success(response, "登录成功");
    }

    /**
     * 用户注册
     */
    @PassToken
    @PostMapping("/register")
    public CommonResult<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        UserVO userVO = authService.register(request);
        return CommonResult.success(userVO, "注册成功");
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public CommonResult<Void> logout() {
        Long userId = UserContext.getUserId();
        authService.logout(userId);
        return CommonResult.success(null, "退出登录成功");
    }
}
