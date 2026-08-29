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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺中心控制器 (商家端 & 商城前台公开端)
 */
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * 申请入驻开店 (主体认证校验、录入资料)
     */
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
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<ShopVO> getShopById(@PathVariable("id") Long id) {
        ShopVO vo = shopService.getShopById(id);
        return CommonResult.success(vo);
    }

    /**
     * 商城公开端分页浏览店铺列表
     */
    @GetMapping("/page")
    @PassToken
    public CommonResult<CommonPage<ShopVO>> pagePublicShops(ShopQueryDTO queryDTO) {
        CommonPage<ShopVO> page = shopService.pagePublicShops(queryDTO);
        return CommonResult.success(page);
    }
}
