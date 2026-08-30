package com.example.baseboot.module.user.cert.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运营端批量审核实名认证请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertBatchAuditDTO {

    /**
     * 认证记录ID列表
     */
    @NotEmpty(message = "认证ID列表不能为空")
    private List<Long> ids;

    /**
     * 审核结果: 1-审核通过, 2-审核驳回
     */
    @NotNull(message = "审核结果状态不能为空 (1-通过, 2-驳回)")
    private Integer status;

    /**
     * 审核备注/驳回原因
     */
    private String auditRemark;
}
