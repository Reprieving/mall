package com.example.baseboot.module.admin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.common.utils.PasswordUtils;
import com.example.baseboot.module.admin.user.dto.AdminUserQueryDTO;
import com.example.baseboot.module.admin.user.service.AdminUserService;
import com.example.baseboot.module.admin.user.vo.AdminUserDetailVO;
import com.example.baseboot.module.order.entity.Order;
import com.example.baseboot.module.order.mapper.OrderMapper;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.example.baseboot.module.user.address.entity.UserAddress;
import com.example.baseboot.module.user.address.mapper.UserAddressMapper;
import com.example.baseboot.module.user.address.vo.AddressVO;
import com.example.baseboot.module.user.cert.entity.UserCertification;
import com.example.baseboot.module.user.cert.mapper.UserCertificationMapper;
import com.example.baseboot.module.user.cert.vo.UserCertVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.entity.SysUser;
import com.example.baseboot.module.user.profile.mapper.SysUserMapper;
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
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 运营端买家用户中台服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserMapper sysUserMapper;
    private final OrderMapper orderMapper;
    private final UserCertificationMapper userCertificationMapper;
    private final ShopMapper shopMapper;
    private final UserAddressMapper userAddressMapper;

    @Override
    public CommonPage<UserVO> pageUsers(AdminUserQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new AdminUserQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(SysUser::getNickname, kw)
                    .or().like(SysUser::getUsername, kw)
                    .or().like(SysUser::getEmail, kw)
                    .or().like(SysUser::getPhone, kw));
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartTime() != null) {
            wrapper.ge(SysUser::getCreateTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            wrapper.le(SysUser::getCreateTime, queryDTO.getEndTime());
        }

        wrapper.orderByDesc(SysUser::getId);
        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        if (CollectionUtils.isEmpty(userPage.getRecords())) {
            return CommonPage.restPage(userPage, Collections.emptyList());
        }

        List<UserVO> voList = userPage.getRecords().stream()
                .map(UserVO::fromEntity)
                .collect(Collectors.toList());

        return CommonPage.restPage(userPage, voList);
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "用户ID不能为空");
        }

        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 订单消费聚合
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, id)
                .ne(Order::getStatus, 4));

        int orderCount = orders.size();
        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() >= 1 && o.getStatus() <= 3)
                .map(Order::getPayAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 实名认证
        UserCertification cert = userCertificationMapper.selectOne(new LambdaQueryWrapper<UserCertification>()
                .eq(UserCertification::getUserId, id));

        // 店铺
        Shop shop = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getUserId, id));

        // 地址列表
        List<UserAddress> addresses = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, id)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));

        List<AddressVO> addressVOs = CollectionUtils.isEmpty(addresses) ? Collections.emptyList()
                : addresses.stream().map(AddressVO::fromEntity).collect(Collectors.toList());

        return AdminUserDetailVO.builder()
                .profile(UserVO.fromEntity(user))
                .orderCount(orderCount)
                .totalSpent(totalSpent)
                .certification(UserCertVO.fromEntity(cert))
                .shop(ShopVO.fromEntity(shop))
                .addresses(addressVOs)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long id, Integer status) {
        if (id == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "参数不合法 (0-禁用, 1-启用)");
        }

        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return sysUserMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetUserPassword(Long id, String newPassword) {
        if (id == null || !StringUtils.hasText(newPassword)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "新密码不能为空");
        }

        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        user.setPassword(PasswordUtils.encode(newPassword.trim()));
        user.setUpdateTime(LocalDateTime.now());
        return sysUserMapper.updateById(user) > 0;
    }

    @Override
    public List<AddressVO> getUserAddresses(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }

        List<UserAddress> addresses = userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, id)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));

        if (CollectionUtils.isEmpty(addresses)) {
            return Collections.emptyList();
        }

        return addresses.stream().map(AddressVO::fromEntity).collect(Collectors.toList());
    }
}
