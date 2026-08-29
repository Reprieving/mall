package com.example.baseboot.module.admin.auth.service;

import com.example.baseboot.module.admin.auth.dto.AdminLoginDTO;
import com.example.baseboot.module.admin.auth.vo.AdminInfoVO;
import com.example.baseboot.module.admin.auth.vo.AdminLoginVO;

/**
 * 运营管理员认证服务接口
 */
public interface AdminAuthService {

    /**
     * 管理员登录
     */
    AdminLoginVO login(AdminLoginDTO loginDTO);

    /**
     * 获取当前登录管理员信息与权限清单
     */
    AdminInfoVO getInfo(Long adminId);

    /**
     * 管理员登出
     */
    void logout(Long adminId);
}
