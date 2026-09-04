package com.example.baseboot.module.shop.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.shop.dto.ShopApplyDTO;
import com.example.baseboot.module.shop.dto.ShopQueryDTO;
import com.example.baseboot.module.shop.dto.ShopUpdateDTO;
import com.example.baseboot.module.shop.service.ShopService;
import com.example.baseboot.module.shop.vo.ShopDetailVO;
import com.example.baseboot.module.shop.vo.ShopVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺中心控制器 (商家端 & 商城前台公开端)
 */
@Tag(name = "13. 店铺业务与公开主页 (ShopController)", description = "商家申请入驻开店、我的店铺资料维护、营业状态打烊与商城店铺公开页浏览")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * 申请入驻开店 (主体认证校验、录入资料)
     */
    @Operation(summary = "申请入驻开店", description = "校验买家已通过主体实名认证后，提交店铺名称、Logo、简介与经营类型完成入驻申请")
    @PostMapping("/apply")
    @LoginRequired
    public CommonResult<ShopDetailVO> applyShop(@Valid @RequestBody ShopApplyDTO applyDTO) {
        Long userId = UserContext.getUserId();
        ShopDetailVO detailVO = shopService.applyShop(userId, applyDTO);
        return CommonResult.success(detailVO, "开店申请提交成功，请等待平台审核");
    }

    /**
     * 查询当前登录用户的店铺入驻进度与店铺资料
     */
    @Operation(summary = "查询我的店铺详情", description = "获取当前用户的店铺详情与入驻审批状态（待审核/审核通过/驳回）")
    @GetMapping("/my")
    @LoginRequired
    public CommonResult<ShopDetailVO> getMyShop() {
        Long userId = UserContext.getUserId();
        ShopDetailVO detailVO = shopService.getMyShop(userId);
        return CommonResult.success(detailVO);
    }

    /**
     * 店主修改店铺基本信息 (Logo, Banner, 公告, 客服电话等)
     */
    @Operation(summary = "店主修改店铺信息", description = "店主自主编辑更新店铺 Logo、主视觉 Banner、经营公告与客服联系电话")
    @PutMapping("/my")
    @LoginRequired
    public CommonResult<ShopVO> updateMyShop(@Valid @RequestBody ShopUpdateDTO updateDTO) {
        Long userId = UserContext.getUserId();
        ShopVO vo = shopService.updateMyShop(userId, updateDTO);
        return CommonResult.success(vo, "店铺信息修改成功");
    }

    /**
     * 店主自主切换营业状态 (1-正常营业, 2-打烊休息)
     */
    @Operation(summary = "切换店铺营业状态", description = "店主自主设置店铺正常营业 (1) 或打烊休息 (2)")
    @PutMapping("/my/status")
    @LoginRequired
    public CommonResult<Void> updateMyShopStatus(@RequestParam("status") Integer status) {
        Long userId = UserContext.getUserId();
        boolean success = shopService.updateMyShopStatus(userId, status);
        if (success) {
            return CommonResult.success(null, "店铺营业状态更新成功");
        }
        return CommonResult.failed("店铺营业状态更新失败");
    }

    /**
     * 查询店铺公开主页信息 (商城前台)
     */
    @Operation(summary = "查询店铺公开主页", description = "商城前台买家查看店铺的基本公开展示信息与综合评分")
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<ShopVO> getShopById(@PathVariable("id") Long id) {
        ShopVO vo = shopService.getShopById(id);
        return CommonResult.success(vo);
    }

    /**
     * 商城公开端分页浏览店铺列表
     */
    @Operation(summary = "商城公开分页浏览店铺", description = "按店铺名称模糊检索与综合排序分页浏览商城入驻店铺")
    @GetMapping("/page")
    @PassToken
    public CommonResult<CommonPage<ShopVO>> pagePublicShops(ShopQueryDTO queryDTO) {
        CommonPage<ShopVO> page = shopService.pagePublicShops(queryDTO);
        return CommonResult.success(page);
    }
}
