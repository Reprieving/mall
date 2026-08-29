package com.example.baseboot.module.admin.user.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.admin.user.dto.AdminUserQueryDTO;
import com.example.baseboot.module.admin.user.service.AdminUserService;
import com.example.baseboot.module.admin.user.vo.AdminUserDetailVO;
import com.example.baseboot.module.user.address.vo.AddressVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营端买家用户管控控制器
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 多维组合分页检索买家列表
     */
    @GetMapping("/page")
    @RequirePermission("user:view")
    public CommonResult<CommonPage<UserVO>> pageUsers(AdminUserQueryDTO queryDTO) {
        CommonPage<UserVO> page = adminUserService.pageUsers(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 获取买家全景画像档案
     */
    @GetMapping("/{id}/detail")
    @RequirePermission("user:view")
    public CommonResult<AdminUserDetailVO> getUserDetail(@PathVariable("id") Long id) {
        AdminUserDetailVO detailVO = adminUserService.getUserDetail(id);
        return CommonResult.success(detailVO);
    }

    /**
     * 封禁 / 解封买家账号
     */
    @PutMapping("/{id}/status")
    @RequirePermission("user:status")
    public CommonResult<Void> updateUserStatus(@PathVariable("id") Long id,
                                               @RequestParam("status") Integer status) {
        boolean success = adminUserService.updateUserStatus(id, status);
        if (success) {
            return CommonResult.success(null, "用户状态更新成功");
        }
        return CommonResult.failed("用户状态更新失败");
    }

    /**
     * 重置买家登录密码
     */
    @PutMapping("/{id}/reset-pwd")
    @RequirePermission("user:reset-pwd")
    public CommonResult<Void> resetUserPassword(@PathVariable("id") Long id,
                                                @RequestParam("password") String password) {
        boolean success = adminUserService.resetUserPassword(id, password);
        if (success) {
            return CommonResult.success(null, "用户密码重置成功");
        }
        return CommonResult.failed("用户密码重置失败");
    }

    /**
     * 查询指定买家的收货地址列表
     */
    @GetMapping("/{id}/addresses")
    @RequirePermission("user:view")
    public CommonResult<List<AddressVO>> getUserAddresses(@PathVariable("id") Long id) {
        List<AddressVO> list = adminUserService.getUserAddresses(id);
        return CommonResult.success(list);
    }
}
