package com.example.baseboot.module.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.shop.dto.ShopApplyDTO;
import com.example.baseboot.module.shop.dto.ShopAuditDTO;
import com.example.baseboot.module.shop.dto.ShopQueryDTO;
import com.example.baseboot.module.shop.dto.ShopUpdateDTO;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import com.example.baseboot.module.shop.service.ShopService;
import com.example.baseboot.module.shop.vo.ShopDetailVO;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.mapper.UserCertificationMapper;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺核心业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    private final UserCertificationMapper userCertificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopDetailVO applyShop(Long userId, ShopApplyDTO applyDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (applyDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "开店参数不能为空");
        }

        // 1. 校验用户是否已开店或提交过申请
        Shop existingShop = this.getOne(new LambdaQueryWrapper<Shop>().eq(Shop::getUserId, userId));
        if (existingShop != null) {
            if (existingShop.getStatus() == 1) {
                throw new BusinessException(ResultCode.SHOP_ALREADY_OPENED, "您已拥有正常营业的店铺");
            }
            if (existingShop.getStatus() == 0) {
                throw new BusinessException(ResultCode.SHOP_ALREADY_OPENED, "您的开店申请正在审核中，请耐心等待");
            }
            if (existingShop.getStatus() == 4) {
                throw new BusinessException(ResultCode.SHOP_STATUS_ERROR, "您的账号已被禁止开店");
            }
        }

        // 2. 校验实名/主体认证
        UserCertification cert = userCertificationMapper.selectOne(new LambdaQueryWrapper<UserCertification>()
                .eq(UserCertification::getUserId, userId)
                .eq(UserCertification::getStatus, 1));
        if (cert == null) {
            throw new BusinessException(ResultCode.SHOP_CERT_REQUIRED);
        }

        // 3. 校验店铺类型与主体认证匹配度
        int shopType = applyDTO.getType();
        int certType = cert.getCertType() != null ? cert.getCertType() : 1;

        if (shopType == 2 && certType < 2) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "开设个体工商户店铺需要先完成个体工商户或企业认证");
        }
        if ((shopType == 3 || shopType == 4) && certType != 3) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "开设企业旗舰店/专营店必须先完成企业实名认证");
        }

        // 4. 校验店铺名称唯一性
        String shopName = applyDTO.getName().trim();
        long nameCount = this.count(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getName, shopName)
                .ne(existingShop != null, Shop::getId, existingShop != null ? existingShop.getId() : 0L));
        if (nameCount > 0) {
            throw new BusinessException(ResultCode.SHOP_NAME_EXISTS);
        }

        Shop shop = existingShop != null ? existingShop : new Shop();
        shop.setUserId(userId);
        shop.setCertId(cert.getId());
        shop.setName(shopName);
        shop.setLogo(applyDTO.getLogo().trim());
        shop.setBanner(applyDTO.getBanner());
        shop.setIntro(applyDTO.getIntro());
        shop.setNotice(applyDTO.getNotice());
        shop.setPhone(applyDTO.getPhone().trim());
        shop.setType(shopType);
        shop.setStatus(0); // 0-待审核
        shop.setRejectReason(null);
        shop.setScore(new BigDecimal("5.00"));
        shop.setUpdateTime(LocalDateTime.now());

        if (existingShop == null) {
            shop.setCreateTime(LocalDateTime.now());
            this.save(shop);
        } else {
            this.updateById(shop);
        }

        return ShopDetailVO.builder()
                .shopInfo(ShopVO.fromEntity(shop))
                .certInfo(UserCertVO.fromEntity(cert))
                .build();
    }

    @Override
    public ShopDetailVO getMyShop(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Shop shop = this.getOne(new LambdaQueryWrapper<Shop>().eq(Shop::getUserId, userId));
        if (shop == null) {
            return null;
        }

        UserCertification cert = shop.getCertId() != null ? userCertificationMapper.selectById(shop.getCertId()) : null;

        return ShopDetailVO.builder()
                .shopInfo(ShopVO.fromEntity(shop))
                .certInfo(UserCertVO.fromEntity(cert))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopVO updateMyShop(Long userId, ShopUpdateDTO updateDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (updateDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "修改参数不能为空");
        }

        Shop shop = this.getOne(new LambdaQueryWrapper<Shop>().eq(Shop::getUserId, userId));
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        String newName = updateDTO.getName().trim();
        if (!newName.equalsIgnoreCase(shop.getName())) {
            long count = this.count(new LambdaQueryWrapper<Shop>()
                    .eq(Shop::getName, newName)
                    .ne(Shop::getId, shop.getId()));
            if (count > 0) {
                throw new BusinessException(ResultCode.SHOP_NAME_EXISTS);
            }
            shop.setName(newName);
        }

        shop.setLogo(updateDTO.getLogo().trim());
        shop.setBanner(updateDTO.getBanner());
        shop.setIntro(updateDTO.getIntro());
        shop.setNotice(updateDTO.getNotice());
        shop.setPhone(updateDTO.getPhone().trim());
        shop.setUpdateTime(LocalDateTime.now());

        this.updateById(shop);
        return ShopVO.fromEntity(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMyShopStatus(Long userId, Integer status) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (1-正常营业, 2-打烊休息)");
        }

        Shop shop = this.getOne(new LambdaQueryWrapper<Shop>().eq(Shop::getUserId, userId));
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        if (shop.getStatus() != 1 && shop.getStatus() != 2) {
            throw new BusinessException(ResultCode.SHOP_STATUS_ERROR, "店铺未审核通过或已被封禁，无法切换营业状态");
        }

        shop.setStatus(status);
        shop.setUpdateTime(LocalDateTime.now());
        return this.updateById(shop);
    }

    @Override
    public ShopVO getShopById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "店铺ID不能为空");
        }
        Shop shop = this.getById(id);
        if (shop == null || (shop.getStatus() != 1 && shop.getStatus() != 2)) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }
        return ShopVO.fromEntity(shop);
    }

    @Override
    public CommonPage<ShopVO> pagePublicShops(ShopQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new ShopQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Shop> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .in(Shop::getStatus, 1, 2); // 正常营业与打烊的店铺公开展示

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Shop::getName, queryDTO.getName().trim());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(Shop::getType, queryDTO.getType());
        }

        wrapper.orderByDesc(Shop::getScore).orderByDesc(Shop::getId);
        Page<Shop> shopPage = this.page(page, wrapper);

        if (CollectionUtils.isEmpty(shopPage.getRecords())) {
            return CommonPage.restPage(shopPage, Collections.emptyList());
        }

        List<ShopVO> voList = shopPage.getRecords().stream().map(ShopVO::fromEntity).collect(Collectors.toList());
        return CommonPage.restPage(shopPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopDetailVO auditShop(ShopAuditDTO auditDTO) {
        if (auditDTO == null || auditDTO.getId() == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "店铺ID不能为空");
        }

        Shop shop = this.getById(auditDTO.getId());
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        int status = auditDTO.getStatus();
        if (status != 1 && status != 3) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "审核状态非法 (1-通过, 3-驳回)");
        }

        if (status == 3 && !StringUtils.hasText(auditDTO.getRejectReason())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "审核驳回时必须填写驳回原因");
        }

        shop.setStatus(status);
        shop.setRejectReason(auditDTO.getRejectReason());
        shop.setUpdateTime(LocalDateTime.now());
        this.updateById(shop);

        return getAdminShopDetail(shop.getId());
    }

    @Override
    public ShopDetailVO getAdminShopDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "店铺ID不能为空");
        }
        Shop shop = this.getById(id);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        UserCertification cert = shop.getCertId() != null ? userCertificationMapper.selectById(shop.getCertId()) : null;

        return ShopDetailVO.builder()
                .shopInfo(ShopVO.fromEntity(shop))
                .certInfo(UserCertVO.fromEntity(cert))
                .build();
    }

    @Override
    public CommonPage<ShopVO> pageAdminShops(ShopQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new ShopQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Shop> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Shop::getName, queryDTO.getName().trim());
        }
        if (queryDTO.getType() != null) {
            wrapper.eq(Shop::getType, queryDTO.getType());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Shop::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(Shop::getPhone, queryDTO.getPhone().trim());
        }

        wrapper.orderByDesc(Shop::getId);
        Page<Shop> shopPage = this.page(page, wrapper);

        if (CollectionUtils.isEmpty(shopPage.getRecords())) {
            return CommonPage.restPage(shopPage, Collections.emptyList());
        }

        List<ShopVO> voList = shopPage.getRecords().stream().map(ShopVO::fromEntity).collect(Collectors.toList());
        return CommonPage.restPage(shopPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdminShopStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "店铺ID不能为空");
        }
        if (status == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值不能为空");
        }

        Shop shop = this.getById(id);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        shop.setStatus(status);
        shop.setUpdateTime(LocalDateTime.now());
        return this.updateById(shop);
    }
}
