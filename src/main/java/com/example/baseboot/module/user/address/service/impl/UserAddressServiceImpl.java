package com.example.baseboot.module.user.address.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.user.address.dto.AddressCreateDTO;
import com.example.baseboot.module.user.address.dto.AddressUpdateDTO;
import com.example.baseboot.module.user.address.entity.UserAddress;
import com.example.baseboot.module.user.address.mapper.UserAddressMapper;
import com.example.baseboot.module.user.address.service.UserAddressService;
import com.example.baseboot.module.user.address.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收货地址业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO createAddress(Long userId, AddressCreateDTO createDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "地址参数不能为空");
        }

        long existingCount = this.count(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId));

        int isDefault = (createDTO.getIsDefault() != null && createDTO.getIsDefault() == 1) || existingCount == 0 ? 1 : 0;

        if (isDefault == 1) {
            resetUserDefaultAddress(userId);
        }

        UserAddress address = UserAddress.builder()
                .userId(userId)
                .name(createDTO.getName().trim())
                .phone(createDTO.getPhone().trim())
                .province(createDTO.getProvince().trim())
                .city(createDTO.getCity().trim())
                .district(createDTO.getDistrict().trim())
                .detailAddress(createDTO.getDetailAddress().trim())
                .postalCode(createDTO.getPostalCode())
                .isDefault(isDefault)
                .tag(createDTO.getTag())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(address);
        return AddressVO.fromEntity(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressVO updateAddress(Long userId, Long id, AddressUpdateDTO updateDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "地址ID不能为空");
        }

        UserAddress address = this.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        int isDefault = updateDTO.getIsDefault() != null ? updateDTO.getIsDefault() : address.getIsDefault();
        if (isDefault == 1 && address.getIsDefault() == 0) {
            resetUserDefaultAddress(userId);
        }

        address.setName(updateDTO.getName().trim());
        address.setPhone(updateDTO.getPhone().trim());
        address.setProvince(updateDTO.getProvince().trim());
        address.setCity(updateDTO.getCity().trim());
        address.setDistrict(updateDTO.getDistrict().trim());
        address.setDetailAddress(updateDTO.getDetailAddress().trim());
        address.setPostalCode(updateDTO.getPostalCode());
        address.setIsDefault(isDefault);
        address.setTag(updateDTO.getTag());
        address.setUpdateTime(LocalDateTime.now());

        this.updateById(address);
        return AddressVO.fromEntity(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long userId, Long id) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "地址ID不能为空");
        }

        UserAddress address = this.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        boolean wasDefault = address.getIsDefault() == 1;
        boolean removed = this.removeById(id);

        // 如果删除的是默认地址，自动将最近创建的地址提升为默认
        if (removed && wasDefault) {
            UserAddress nextAddress = this.getOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getId)
                    .last("LIMIT 1"));
            if (nextAddress != null) {
                nextAddress.setIsDefault(1);
                nextAddress.setUpdateTime(LocalDateTime.now());
                this.updateById(nextAddress);
            }
        }

        return removed;
    }

    @Override
    public AddressVO getAddressById(Long userId, Long id) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "地址ID不能为空");
        }

        UserAddress address = this.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        return AddressVO.fromEntity(address);
    }

    @Override
    public List<AddressVO> listUserAddresses(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        List<UserAddress> list = this.list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getId));

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream().map(AddressVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public AddressVO getDefaultAddress(Long userId) {
        if (userId == null) {
            return null;
        }

        UserAddress address = this.getOne(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getIsDefault, 1));

        if (address == null) {
            address = this.getOne(new LambdaQueryWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .orderByDesc(UserAddress::getId)
                    .last("LIMIT 1"));
        }

        return AddressVO.fromEntity(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long userId, Long id) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "地址ID不能为空");
        }

        UserAddress address = this.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }

        resetUserDefaultAddress(userId);

        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        return this.updateById(address);
    }

    private void resetUserDefaultAddress(Long userId) {
        this.lambdaUpdate()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0)
                .update();
    }
}
