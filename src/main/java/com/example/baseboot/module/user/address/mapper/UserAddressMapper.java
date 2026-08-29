package com.example.baseboot.module.user.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.baseboot.module.user.address.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址 Mapper 接口
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
