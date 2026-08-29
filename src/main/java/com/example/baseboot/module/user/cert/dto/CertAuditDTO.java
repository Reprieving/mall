package com.example.baseboot.module.user.cert.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核认证申请请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertAuditDTO {

    /**
     * 认证记录ID
     */
    @NotNull(message = "认证记录ID不能为空")
    private Long id;

    /**
     * 审核结果: 1-审核通过, 2-审核驳回
     */
    @NotNull(message = "审核结果不能为空 (1-通过, 2-驳回)")
    private Integer status;

    /**
     * 审核批注/驳回原因 (驳回时必填)
     */
    private String auditRemark;
}
