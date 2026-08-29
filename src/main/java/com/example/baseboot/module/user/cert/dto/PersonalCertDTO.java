package com.example.baseboot.module.user.cert.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个人实名认证提交参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalCertDTO {

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 18位二代身份证号码
     */
    @NotBlank(message = "身份证号码不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])\\d{3}[\\dXx]$", message = "身份证号码格式不合法")
    private String idCard;

    /**
     * 身份证人像面照片URL
     */
    @NotBlank(message = "请上传身份证正面（人像面）照片")
    private String idCardFrontPic;

    /**
     * 身份证国徽面照片URL
     */
    @NotBlank(message = "请上传身份证反面（国徽面）照片")
    private String idCardBackPic;
}
