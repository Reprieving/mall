package com.example.baseboot.module.user.cert.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.user.cert.dto.*;
import com.example.baseboot.module.user.cert.service.CertificationService;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户主体实名认证控制器 (支持个人、个体户、企业)
 */
@RestController
@RequestMapping("/api/user/cert")
@RequiredArgsConstructor
@LoginRequired
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * 提交个人实名认证
     */
    @PostMapping("/personal")
    public CommonResult<UserCertVO> submitPersonalCert(@Valid @RequestBody PersonalCertDTO certDTO) {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.submitPersonalCert(userId, certDTO);
        return CommonResult.success(certVO, "个人实名认证提交成功，请等待审核");
    }

    /**
     * 提交个体工商户认证
     */
    @PostMapping("/individual")
    public CommonResult<UserCertVO> submitIndividualCert(@Valid @RequestBody IndividualCertDTO certDTO) {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.submitIndividualCert(userId, certDTO);
        return CommonResult.success(certVO, "个体工商户认证提交成功，请等待审核");
    }

    /**
     * 提交企业实名认证
     */
    @PostMapping("/enterprise")
    public CommonResult<UserCertVO> submitEnterpriseCert(@Valid @RequestBody EnterpriseCertDTO certDTO) {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.submitEnterpriseCert(userId, certDTO);
        return CommonResult.success(certVO, "企业实名认证提交成功，请等待审核");
    }

    /**
     * 查询当前用户的实名认证状态与详情
     */
    @GetMapping("/my")
    public CommonResult<UserCertVO> getMyCertification() {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.getCertificationByUserId(userId);
        return CommonResult.success(certVO);
    }

    /**
     * 审核实名认证申请 (通过 / 驳回)
     */
    @PostMapping("/audit")
    public CommonResult<UserCertVO> auditCertification(@Valid @RequestBody CertAuditDTO auditDTO) {
        UserCertVO certVO = certificationService.auditCertification(auditDTO);
        return CommonResult.success(certVO, "实名认证审核操作完成");
    }

    /**
     * 根据用户 ID 查询实名认证信息 (管理端)
     */
    @GetMapping("/{userId}")
    public CommonResult<UserCertVO> getCertByUserId(@PathVariable("userId") Long userId) {
        UserCertVO certVO = certificationService.getCertificationByUserId(userId);
        return CommonResult.success(certVO);
    }

    /**
     * 分页多条件查询认证审核列表 (管理端)
     */
    @GetMapping("/page")
    public CommonResult<CommonPage<UserCertVO>> pageCertifications(CertQueryDTO queryDTO) {
        CommonPage<UserCertVO> page = certificationService.pageCertifications(queryDTO);
        return CommonResult.success(page);
    }
}
