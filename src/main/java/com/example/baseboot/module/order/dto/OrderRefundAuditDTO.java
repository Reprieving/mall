package com.example.baseboot.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营端/卖家审批退款申请参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundAuditDTO {

    /**
     * 审批状态: 1-审核通过(同意退款并关闭订单), 2-审核驳回(拒绝退款)
     */
    @NotNull(message = "审批状态不能为空 (1-审核通过, 2-审核驳回)")
    private Integer status;

    /**
     * 审批批注 / 驳回原因说明
     */
    private String auditRemark;
}
