package com.example.baseboot.module.user.cert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业实名认证提交参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseCertDTO {

    /**
     * 企业法定代表人姓名
     */
    @NotBlank(message = "法定代表人姓名不能为空")
    private String realName;

    /**
     * 法定代表人身份证号码
     */
    @NotBlank(message = "法定代表人身份证号码不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])\\d{3}[\\dXx]$", message = "身份证号码格式不合法")
    private String idCard;

    /**
     * 法人身份证人像面照片URL
     */
    @NotBlank(message = "请上传法人身份证人像面照片")
    private String idCardFrontPic;

    /**
     * 法人身份证国徽面照片URL
     */
    @NotBlank(message = "请上传法人身份证国徽面照片")
    private String idCardBackPic;

    /**
     * 企业全称 (营业执照主体名称)
     */
    @NotBlank(message = "企业全称不能为空")
    private String companyName;

    /**
     * 统一社会信用代码 (18位)
     */
    @NotBlank(message = "统一社会信用代码不能为空")
    @Pattern(regexp = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$", message = "统一社会信用代码格式不合法")
    private String businessLicenseNo;

    /**
     * 营业执照副本扫描件/照片URL
     */
    @NotBlank(message = "请上传营业执照电子扫描件")
    private String businessLicensePic;

    /**
     * 企业注册住所 / 经营地址
     */
    @NotBlank(message = "企业注册地址不能为空")
    private String companyAddress;
}
