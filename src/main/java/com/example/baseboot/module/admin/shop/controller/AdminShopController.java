package com.example.baseboot.module.admin.shop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.admin.shop.dto.ShopBatchStatusDTO;
import com.example.baseboot.module.admin.shop.vo.ShopAdminFullDetailVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.service.ShopService;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.mapper.UserCertificationMapper;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.entity.SysUser;
import com.example.baseboot.module.user.profile.mapper.SysUserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运营端店铺批量管控与综合详情控制器
 */
@RestController
@RequestMapping("/api/admin/shop")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;
    private final UserCertificationMapper certificationMapper;
    private final SysUserMapper sysUserMapper;
    private final SpuMapper spuMapper;

    /**
     * 批量管控店铺营业/封禁状态
     */
    @PutMapping("/batch-status")
    @RequirePermission("shop:status")
    public CommonResult<Integer> batchUpdateShopStatus(@Valid @RequestBody ShopBatchStatusDTO batchDTO) {
        int count = 0;
        for (Long id : batchDTO.getIds()) {
            try {
                boolean ok = shopService.updateAdminShopStatus(id, batchDTO.getStatus());
                if (ok) {
                    count++;
                }
            } catch (Exception ignored) {
            }
        }
        return CommonResult.success(count, "批量操作完成，成功处理 " + count + " 个店铺");
    }

    /**
     * 人工修正店铺综合评分
     */
    @PutMapping("/{id}/score")
    @RequirePermission("shop:score")
    public CommonResult<Void> updateShopScore(@PathVariable("id") Long id,
                                              @RequestParam("score") BigDecimal score) {
        if (id == null || score == null || score.compareTo(BigDecimal.ONE) < 0 || score.compareTo(new BigDecimal("5.00")) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "评分值必须在 1.00 至 5.00 之间");
        }

        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        shop.setScore(score);
        shop.setUpdateTime(LocalDateTime.now());
        shopService.updateById(shop);
        return CommonResult.success(null, "店铺评分修改成功");
    }

    /**
     * 查看店铺全量全景详情 (包含店铺资料、认证资质、店主信息与商品数)
     */
    @GetMapping("/{id}/full-detail")
    @RequirePermission("shop:view")
    public CommonResult<ShopAdminFullDetailVO> getShopFullDetail(@PathVariable("id") Long id) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        UserCertification cert = shop.getCertId() != null ? certificationMapper.selectById(shop.getCertId()) : null;
        SysUser owner = sysUserMapper.selectById(shop.getUserId());
        Long productCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getShopId, id));

        ShopAdminFullDetailVO vo = ShopAdminFullDetailVO.builder()
                .shop(ShopVO.fromEntity(shop))
                .certification(UserCertVO.fromEntity(cert))
                .owner(UserVO.fromEntity(owner))
                .productCount(productCount)
                .build();

        return CommonResult.success(vo);
    }
}
