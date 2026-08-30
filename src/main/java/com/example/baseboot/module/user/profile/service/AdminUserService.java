package com.example.baseboot.module.user.profile.service;

import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.user.profile.dto.AdminUserQueryDTO;
import com.example.baseboot.module.user.profile.vo.AdminUserDetailVO;
import com.example.baseboot.module.user.address.vo.AddressVO;
import com.example.baseboot.module.user.profile.dto.UserVO;

import java.util.List;

/**
 * 运营端买家用户中台服务接口
 */
public interface AdminUserService {

    /**
     * 多维分页检索买家列表
     */
    CommonPage<UserVO> pageUsers(AdminUserQueryDTO queryDTO);

    /**
     * 获取买家全景画像档案
     */
    AdminUserDetailVO getUserDetail(Long id);

    /**
     * 封禁 / 解封买家账号
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 重置买家登录密码
     */
    boolean resetUserPassword(Long id, String newPassword);

    /**
     * 查看买家的收货地址列表
     */
    List<AddressVO> getUserAddresses(Long id);
}
