package com.example.baseboot.module.user.cert.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.user.cert.dto.CertBatchAuditDTO;
import com.example.baseboot.module.user.cert.dto.CertAuditDTO;
import com.example.baseboot.module.user.cert.service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营端主体实名认证批量管控控制器
 */
@Tag(name = "21. 运营认证批量审批 (AdminCertController)", description = "运营后台对用户个人/个体/企业主体实名认证进行批量审批通过或驳回")
@RestController
@RequestMapping("/api/admin/cert")
@RequiredArgsConstructor
public class AdminCertController {

    private final CertificationService certificationService;

    /**
     * 批量审核主体实名认证申请
     */
    @Operation(summary = "批量审核实名认证申请", description = "支持勾选多条主体实名认证申请记录，一次性批量执行通过 (1) 或驳回 (2)")
    @PostMapping("/batch-audit")
    @RequirePermission("cert:audit")
    public CommonResult<Integer> batchAudit(@Valid @RequestBody CertBatchAuditDTO batchDTO) {
        int count = 0;
        for (Long id : batchDTO.getIds()) {
            try {
                certificationService.auditCertification(CertAuditDTO.builder()
                        .id(id)
                        .status(batchDTO.getStatus())
                        .auditRemark(batchDTO.getAuditRemark())
                        .build());
                count++;
            } catch (Exception ignored) {
            }
        }
        return CommonResult.success(count, "批量审核完成，成功处理 " + count + " 条记录");
    }
}
