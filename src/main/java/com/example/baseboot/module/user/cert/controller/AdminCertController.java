package com.example.baseboot.module.user.cert.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.user.cert.dto.CertBatchAuditDTO;
import com.example.baseboot.module.user.cert.dto.CertAuditDTO;
import com.example.baseboot.module.user.cert.dto.CertQueryDTO;
import com.example.baseboot.module.user.cert.service.CertificationService;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营端主体实名认证批量管控控制器
 */
@Tag(name = "21. 运营认证批量审批 (AdminCertController)", description = "运营后台对用户个人/个体/企业主体实名认证进行批量审批通过或驳回")
@RestController
@RequestMapping("/admin/cert")
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

    /**
     * 根据用户 ID 查询实名认证信息 (管理端)
     */
    @Operation(summary = "按用户ID查询认证信息", description = "管理端通过买家用户 ID 直接调取其名下绑定的主体认证档案")
    @GetMapping("/{userId}")
    public CommonResult<UserCertVO> getCertByUserId(@PathVariable("userId") Long userId) {
        UserCertVO certVO = certificationService.getCertificationByUserId(userId);
        return CommonResult.success(certVO);
    }

    /**
     * 分页多条件查询认证审核列表 (管理端)
     */
    @Operation(summary = "多条件分页查询认证审核列表", description = "按认证类型（个人/个体/企业）、审核状态及申请人关键字多条件检索待审或历史记录")
    @GetMapping("/page")
    public CommonResult<CommonPage<UserCertVO>> pageCertifications(CertQueryDTO queryDTO) {
        CommonPage<UserCertVO> page = certificationService.pageCertifications(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 审核实名认证申请 (通过 / 驳回)
     */
    @Operation(summary = "审批实名认证申请", description = "运营管理员审核资质真实性，执行审核通过 (1) 或驳回 (2) 并记录审核原因")
    @PostMapping("/audit")
    public CommonResult<UserCertVO> auditCertification(@Valid @RequestBody CertAuditDTO auditDTO) {
        UserCertVO certVO = certificationService.auditCertification(auditDTO);
        return CommonResult.success(certVO, "实名认证审核操作完成");
    }
}
