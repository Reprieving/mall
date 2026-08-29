package com.example.baseboot.module.user.cert.vo;

import com.example.baseboot.module.user.cert.entity.UserCertification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户主体实名认证详情视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCertVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Integer certType;
    private String certTypeName;
    private String realName;
    private String idCard;
    private String idCardFrontPic;
    private String idCardBackPic;
    private String companyName;
    private String businessLicenseNo;
    private String businessLicensePic;
    private String companyAddress;
    private Integer status;
    private String statusName;
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;

    public static UserCertVO fromEntity(UserCertification entity) {
        if (entity == null) {
            return null;
        }

        String certTypeName = switch (entity.getCertType() != null ? entity.getCertType() : 1) {
            case 1 -> "个人实名认证";
            case 2 -> "个体工商户认证";
            case 3 -> "企业认证";
            default -> "未知类型";
        };

        String statusName = switch (entity.getStatus() != null ? entity.getStatus() : 0) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "审核驳回";
            default -> "未知状态";
        };

        return UserCertVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .certType(entity.getCertType())
                .certTypeName(certTypeName)
                .realName(entity.getRealName())
                .idCard(entity.getIdCard())
                .idCardFrontPic(entity.getIdCardFrontPic())
                .idCardBackPic(entity.getIdCardBackPic())
                .companyName(entity.getCompanyName())
                .businessLicenseNo(entity.getBusinessLicenseNo())
                .businessLicensePic(entity.getBusinessLicensePic())
                .companyAddress(entity.getCompanyAddress())
                .status(entity.getStatus())
                .statusName(statusName)
                .auditRemark(entity.getAuditRemark())
                .auditTime(entity.getAuditTime())
                .createTime(entity.getCreateTime())
                .build();
    }
}
