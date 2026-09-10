package com.example.baseboot.module.user.profile.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.user.profile.dto.AdminUserQueryDTO;
import com.example.baseboot.module.user.profile.service.AdminUserService;
import com.example.baseboot.module.user.profile.vo.AdminUserDetailVO;
import com.example.baseboot.module.user.address.vo.AddressVO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营端买家用户管控控制器
 */
@Tag(name = "18. 买家用户管控 (AdminUserController)", description = "运营后台买家用户多维检索、用户全景档案、账号封禁解封与密码重置")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 多维组合分页检索买家列表
     */
    @Operation(summary = "运营端分页检索买家", description = "按用户名、邮箱、手机号及状态多条件筛选买家账号列表")
    @GetMapping("/page")
    @RequirePermission("user:view")
    public CommonResult<CommonPage<UserVO>> pageUsers(AdminUserQueryDTO queryDTO) {
        CommonPage<UserVO> page = adminUserService.pageUsers(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 获取买家全景画像档案
     */
    @Operation(summary = "获取买家全景画像档案", description = "查看买家基本资料、主体认证资质、默认收货地址与账号状态详情")
    @GetMapping("/{id}/detail")
    @RequirePermission("user:view")
    public CommonResult<AdminUserDetailVO> getUserDetail(@PathVariable("id") Long id) {
        AdminUserDetailVO detailVO = adminUserService.getUserDetail(id);
        return CommonResult.success(detailVO);
    }

    /**
     * 封禁 / 解封买家账号
     */
    @Operation(summary = "封禁/解封买家账号", description = "将买家账号设置为正常 (1) 或冻结封禁 (0)")
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
    @Operation(summary = "重置买家登录密码", description = "运营人员为买家重置登录密码并进行 BCrypt 加密保存")
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
    @Operation(summary = "查询指定买家收货地址", description = "客服排查纠纷时查看特定买家名下的全部收货地址列表")
    @GetMapping("/{id}/addresses")
    @RequirePermission("user:view")
    public CommonResult<List<AddressVO>> getUserAddresses(@PathVariable("id") Long id) {
        List<AddressVO> list = adminUserService.getUserAddresses(id);
        return CommonResult.success(list);
    }
}
