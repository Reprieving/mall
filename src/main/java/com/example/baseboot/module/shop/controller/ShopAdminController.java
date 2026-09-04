package com.example.baseboot.module.shop.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.shop.dto.ShopAuditDTO;
import com.example.baseboot.module.shop.dto.ShopQueryDTO;
import com.example.baseboot.module.shop.service.ShopService;
import com.example.baseboot.module.shop.vo.ShopDetailVO;
import com.example.baseboot.module.shop.vo.ShopVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 平台管理端店铺运营与审核控制器
 */
@Tag(name = "14. 商户管理与入驻审核 (ShopAdminController)", description = "运营端店铺多条件分页、资质核查、开店审核审批与违规状态封禁")
@RestController
@RequestMapping("/api/shop/admin")
@RequiredArgsConstructor
@LoginRequired
public class ShopAdminController {

    private final ShopService shopService;

    /**
     * 多条件分页检索全量店铺列表
     */
    @Operation(summary = "运营端分页检索店铺", description = "按店铺名称、认证类型、经营类目、审核状态等多条件分页排查店铺")
    @GetMapping("/page")
    public CommonResult<CommonPage<ShopVO>> pageAdminShops(ShopQueryDTO queryDTO) {
        CommonPage<ShopVO> page = shopService.pageAdminShops(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 查询店铺全量资质与审核详情
     */
    @Operation(summary = "查询店铺资质审核详情", description = "查看店铺营业执照资质、法人身份证及开店申请材料明细")
    @GetMapping("/{id}")
    public CommonResult<ShopDetailVO> getAdminShopDetail(@PathVariable("id") Long id) {
        ShopDetailVO detailVO = shopService.getAdminShopDetail(id);
        return CommonResult.success(detailVO);
    }

    /**
     * 审核开店申请 (通过 / 驳回)
     */
    @Operation(summary = "审批开店申请", description = "平台运营审核商家开店入驻申请，执行审批通过 (1) 或驳回 (2) 并记录审核备注")
    @PostMapping("/audit")
    public CommonResult<ShopDetailVO> auditShop(@Valid @RequestBody ShopAuditDTO auditDTO) {
        ShopDetailVO detailVO = shopService.auditShop(auditDTO);
        return CommonResult.success(detailVO, "店铺审核操作成功");
    }

    /**
     * 运营管控店铺状态 (如违规封禁/解封)
     */
    @Operation(summary = "管控店铺状态(封禁/解封)", description = "针对违规商户快速执行关店封禁 (3) 或恢复正常 (1)")
    @PutMapping("/{id}/status")
    public CommonResult<Void> updateAdminShopStatus(@PathVariable("id") Long id,
                                                    @RequestParam("status") Integer status) {
        boolean success = shopService.updateAdminShopStatus(id, status);
        if (success) {
            return CommonResult.success(null, "店铺状态变更成功");
        }
        return CommonResult.failed("店铺状态变更失败");
    }
}
