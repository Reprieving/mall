package com.example.baseboot.module.admin.cert.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.admin.cert.dto.CertBatchAuditDTO;
import com.example.baseboot.module.user.cert.dto.CertAuditDTO;
import com.example.baseboot.module.user.cert.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营端主体实名认证批量管控控制器
 */
@RestController
@RequestMapping("/api/admin/cert")
@RequiredArgsConstructor
public class AdminCertController {

    private final CertificationService certificationService;

    /**
     * 批量审核主体实名认证申请
     */
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
