package com.example.baseboot.module.user.cert.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户主体实名认证实体 (对应表 sys_user_certification)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_certification")
public class UserCertification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 认证记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 认证类型: 1-个人实名认证, 2-个体工商户认证, 3-企业认证
     */
    private Integer certType;

    /**
     * 真实姓名 / 法人姓名 / 经营者姓名
     */
    private String realName;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 身份证人像面照片URL
     */
    private String idCardFrontPic;

    /**
     * 身份证国徽面照片URL
     */
    private String idCardBackPic;

    /**
     * 企业全称 / 个体户字号名称
     */
    private String companyName;

    /**
     * 统一社会信用代码 / 营业执照号
     */
    private String businessLicenseNo;

    /**
     * 营业执照电子扫描件/照片URL
     */
    private String businessLicensePic;

    /**
     * 企业注册地址 / 经营场所
     */
    private String companyAddress;

    /**
     * 审核状态: 0-待审核, 1-审核通过, 2-审核驳回
     */
    private Integer status;

    /**
     * 审核批注/驳回原因
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 提交时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
