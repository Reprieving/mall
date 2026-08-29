package com.example.baseboot.module.user.cert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个体工商户实名认证提交参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualCertDTO {

    /**
     * 经营者姓名
     */
    @NotBlank(message = "经营者姓名不能为空")
    private String realName;

    /**
     * 经营者身份证号码
     */
    @NotBlank(message = "经营者身份证号码不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])\\d{3}[\\dXx]$", message = "身份证号码格式不合法")
    private String idCard;

    /**
     * 经营者身份证人像面照片URL
     */
    @NotBlank(message = "请上传身份证人像面照片")
    private String idCardFrontPic;

    /**
     * 经营者身份证国徽面照片URL
     */
    @NotBlank(message = "请上传身份证国徽面照片")
    private String idCardBackPic;

    /**
     * 个体工商户字号名称 (如: 海淀区朝阳便利店)
     */
    @NotBlank(message = "个体户字号名称不能为空")
    private String companyName;

    /**
     * 统一社会信用代码 / 营业执照注册号
     */
    @NotBlank(message = "统一社会信用代码不能为空")
    private String businessLicenseNo;

    /**
     * 营业执照扫描件/照片URL
     */
    @NotBlank(message = "请上传营业执照电子照片")
    private String businessLicensePic;

    /**
     * 经营场所地址
     */
    private String companyAddress;
}
