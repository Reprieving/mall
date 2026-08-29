package com.example.baseboot.module.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.shop.dto.ShopApplyDTO;
import com.example.baseboot.module.shop.dto.ShopAuditDTO;
import com.example.baseboot.module.shop.dto.ShopQueryDTO;
import com.example.baseboot.module.shop.dto.ShopUpdateDTO;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.vo.ShopDetailVO;
import com.example.baseboot.module.shop.vo.ShopVO;

/**
 * 店铺核心业务接口
 */
public interface ShopService extends IService<Shop> {

    /**
     * 申请开店 (主体认证校验、店铺资料录入)
     */
    ShopDetailVO applyShop(Long userId, ShopApplyDTO applyDTO);

    /**
     * 查询当前用户的店铺信息与入驻进度
     */
    ShopDetailVO getMyShop(Long userId);

    /**
     * 店主修改店铺资料
     */
    ShopVO updateMyShop(Long userId, ShopUpdateDTO updateDTO);

    /**
     * 店主自主切换营业状态 (1-正常营业, 2-打烊休息)
     */
    boolean updateMyShopStatus(Long userId, Integer status);

    /**
     * 查询店铺公开主页信息
     */
    ShopVO getShopById(Long id);

    /**
     * 商城公开端分页浏览店铺
     */
    CommonPage<ShopVO> pagePublicShops(ShopQueryDTO queryDTO);

    /**
     * 管理端审核开店申请 (通过 / 驳回)
     */
    ShopDetailVO auditShop(ShopAuditDTO auditDTO);

    /**
     * 管理端查看店铺完整审核详情 (含主体资质)
     */
    ShopDetailVO getAdminShopDetail(Long id);

    /**
     * 管理端多条件分页查询店铺列表
     */
    CommonPage<ShopVO> pageAdminShops(ShopQueryDTO queryDTO);

    /**
     * 管理端管控店铺状态 (如违规封禁/解封)
     */
    boolean updateAdminShopStatus(Long id, Integer status);
}
