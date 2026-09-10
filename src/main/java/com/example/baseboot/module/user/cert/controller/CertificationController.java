package com.example.baseboot.module.user.cert.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.user.cert.dto.*;
import com.example.baseboot.module.user.cert.service.CertificationService;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户主体实名认证控制器 (支持个人、个体户、企业)
 */
@Tag(name = "20. 主体实名认证 (CertificationController)", description = "用户个人、个体工商户、企业主体三类实名认证资料提交、审核与进度查询")
@RestController
@RequestMapping("/api/user/cert")
@RequiredArgsConstructor
@LoginRequired
public class CertificationController {

    private final CertificationService certificationService;

    /**
     * 提交个人实名认证
     */
    @Operation(summary = "提交个人实名认证", description = "上传本人真实姓名、身份证号码及身份证正反面影印件")
    @PostMapping("/personal")
    public CommonResult<UserCertVO> submitPersonalCert(@Valid @RequestBody PersonalCertDTO certDTO) {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.submitPersonalCert(userId, certDTO);
        return CommonResult.success(certVO, "个人实名认证提交成功，请等待审核");
    }

    /**
     * 提交个体工商户认证
     */
    @Operation(summary = "提交个体工商户认证", description = "上传个体字号、统一社会信用代码、营业执照照片及经营者身份证件")
    @PostMapping("/individual")
    public CommonResult<UserCertVO> submitIndividualCert(@Valid @RequestBody IndividualCertDTO certDTO) {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.submitIndividualCert(userId, certDTO);
        return CommonResult.success(certVO, "个体工商户认证提交成功，请等待审核");
    }

    /**
     * 提交企业实名认证
     */
    @Operation(summary = "提交企业实名认证", description = "上传企业完整名称、统一社会信用代码、营业执照、法定代表人姓名与法人身份证照片")
    @PostMapping("/enterprise")
    public CommonResult<UserCertVO> submitEnterpriseCert(@Valid @RequestBody EnterpriseCertDTO certDTO) {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.submitEnterpriseCert(userId, certDTO);
        return CommonResult.success(certVO, "企业实名认证提交成功，请等待审核");
    }

    /**
     * 查询当前用户的实名认证状态与详情
     */
    @Operation(summary = "查询我的实名认证", description = "查看当前登录账号的主体认证类型、提交资料与当前审核流转状态")
    @GetMapping("/my")
    public CommonResult<UserCertVO> getMyCertification() {
        Long userId = UserContext.getUserId();
        UserCertVO certVO = certificationService.getCertificationByUserId(userId);
        return CommonResult.success(certVO);
    }




}
