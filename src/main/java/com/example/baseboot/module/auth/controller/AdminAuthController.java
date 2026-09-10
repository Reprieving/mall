package com.example.baseboot.module.auth.controller;

import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.AdminContext;
import com.example.baseboot.module.auth.dto.AdminLoginDTO;
import com.example.baseboot.module.auth.service.AdminAuthService;
import com.example.baseboot.module.auth.vo.AdminInfoVO;
import com.example.baseboot.module.auth.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运营管理员认证控制器
 */
@Tag(name = "02. 运营管理员认证 (AdminAuthController)", description = "平台运营管理员登录、信息与权限清单获取、退出登录")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 运营管理员登录
     */
    @Operation(summary = "运营管理员登录", description = "平台运营人员通过账号密码登录，获取专属管理凭证与 RBAC 权限集合")
    @PostMapping("/login")
    @PassToken
    public CommonResult<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AdminLoginVO vo = adminAuthService.login(loginDTO);
        return CommonResult.success(vo, "管理员登录成功");
    }

    /**
     * 获取当前登录管理员信息与权限清单
     */
    @Operation(summary = "获取当前管理员信息与权限清单", description = "解析管理员 Token 并返回账号详情、所属角色与可操作权限标识列表")
    @GetMapping("/info")
    public CommonResult<AdminInfoVO> getInfo() {
        Long adminId = AdminContext.getAdminId();
        AdminInfoVO vo = adminAuthService.getInfo(adminId);
        return CommonResult.success(vo);
    }

    /**
     * 退出登录
     */
    @Operation(summary = "运营管理员退出登录", description = "使管理员 Token 和权限缓存立即失效")
    @PostMapping("/logout")
    public CommonResult<Void> logout() {
        Long adminId = AdminContext.getAdminId();
        adminAuthService.logout(adminId);
        return CommonResult.success(null, "退出登录成功");
    }
}
