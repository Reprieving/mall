package com.example.baseboot.module.auth.controller;

import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.AdminContext;
import com.example.baseboot.module.auth.dto.AdminLoginDTO;
import com.example.baseboot.module.auth.service.AdminAuthService;
import com.example.baseboot.module.auth.vo.AdminInfoVO;
import com.example.baseboot.module.auth.vo.AdminLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 运营管理员认证控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 运营管理员登录
     */
    @PostMapping("/login")
    @PassToken
    public CommonResult<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO loginDTO) {
        AdminLoginVO vo = adminAuthService.login(loginDTO);
        return CommonResult.success(vo, "管理员登录成功");
    }

    /**
     * 获取当前登录管理员信息与权限清单
     */
    @GetMapping("/info")
    public CommonResult<AdminInfoVO> getInfo() {
        Long adminId = AdminContext.getAdminId();
        AdminInfoVO vo = adminAuthService.getInfo(adminId);
        return CommonResult.success(vo);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public CommonResult<Void> logout() {
        Long adminId = AdminContext.getAdminId();
        adminAuthService.logout(adminId);
        return CommonResult.success(null, "退出登录成功");
    }
}
