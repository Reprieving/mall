package com.example.baseboot.module.user.address.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.module.user.address.dto.AddressCreateDTO;
import com.example.baseboot.module.user.address.dto.AddressUpdateDTO;
import com.example.baseboot.module.user.address.entity.UserAddress;
import com.example.baseboot.module.user.address.vo.AddressVO;

import java.util.List;

/**
 * 用户收货地址业务接口
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 新增收货地址
     */
    AddressVO createAddress(Long userId, AddressCreateDTO createDTO);

    /**
     * 修改收货地址
     */
    AddressVO updateAddress(Long userId, Long id, AddressUpdateDTO updateDTO);

    /**
     * 删除收货地址
     */
    boolean deleteAddress(Long userId, Long id);

    /**
     * 获取单个收货地址详情
     */
    AddressVO getAddressById(Long userId, Long id);

    /**
     * 获取当前用户的所有收货地址 (默认地址置顶)
     */
    List<AddressVO> listUserAddresses(Long userId);

    /**
     * 获取当前用户的默认收货地址
     */
    AddressVO getDefaultAddress(Long userId);

    /**
     * 设为默认收货地址
     */
    boolean setDefaultAddress(Long userId, Long id);
}
