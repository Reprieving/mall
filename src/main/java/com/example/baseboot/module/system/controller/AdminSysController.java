package com.example.baseboot.module.system.controller;

import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.system.dto.AdminUserCreateDTO;
import com.example.baseboot.module.system.dto.AdminUserUpdateDTO;
import com.example.baseboot.module.system.dto.RoleDTO;
import com.example.baseboot.module.system.service.AdminSysService;
import com.example.baseboot.module.system.vo.AdminUserVO;
import com.example.baseboot.module.system.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运营端系统管理控制器 (管理员账号 & RBAC 角色配置)
 */
@Tag(name = "16. 运营系统管理与RBAC (AdminSysController)", description = "运营后台管理员账号增删改查、状态启停、重置密码及 RBAC 角色权限配置")
@RestController
@RequestMapping("/admin/sys")
@RequiredArgsConstructor
public class AdminSysController {

    private final AdminSysService adminSysService;

    /**
     * 分页查询管理员列表
     */
    @Operation(summary = "分页查询管理员列表", description = "按账号/姓名关键字、所属角色及状态筛选管理员用户")
    @GetMapping("/user/page")
    @RequirePermission("sys:user:view")
    public CommonResult<CommonPage<AdminUserVO>> pageAdminUsers(@RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                                                @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
                                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                                @RequestParam(value = "roleId", required = false) Long roleId,
                                                                @RequestParam(value = "status", required = false) Integer status) {
        CommonPage<AdminUserVO> page = adminSysService.pageAdminUsers(pageNum, pageSize, keyword, roleId, status);
        return CommonResult.success(page);
    }

    /**
     * 新增管理员账号
     */
    @Operation(summary = "新增管理员账号", description = "创建新后台运营账号，使用 BCrypt 加密存储密码并赋予系统角色")
    @PostMapping("/user")
    @RequirePermission("sys:user:add")
    public CommonResult<AdminUserVO> createAdminUser(@Valid @RequestBody AdminUserCreateDTO createDTO) {
        AdminUserVO vo = adminSysService.createAdminUser(createDTO);
        return CommonResult.success(vo, "管理员创建成功");
    }

    /**
     * 编辑管理员账号
     */
    @Operation(summary = "编辑管理员账号", description = "修改管理员昵称、头像、邮箱、手机号及关联角色")
    @PutMapping("/user/{id}")
    @RequirePermission("sys:user:edit")
    public CommonResult<AdminUserVO> updateAdminUser(@PathVariable("id") Long id,
                                                     @Valid @RequestBody AdminUserUpdateDTO updateDTO) {
        AdminUserVO vo = adminSysService.updateAdminUser(id, updateDTO);
        return CommonResult.success(vo, "管理员修改成功");
    }

    /**
     * 启用/停用管理员账号
     */
    @Operation(summary = "启用/停用管理员账号", description = "控制管理员账号是否允许登录后台系统 (1-启用, 0-停用)")
    @PutMapping("/user/{id}/status")
    @RequirePermission("sys:user:status")
    public CommonResult<Void> updateAdminUserStatus(@PathVariable("id") Long id,
                                                    @RequestParam("status") Integer status) {
        boolean success = adminSysService.updateAdminUserStatus(id, status);
        if (success) {
            return CommonResult.success(null, "状态更新成功");
        }
        return CommonResult.failed("状态更新失败");
    }

    /**
     * 重置管理员密码
     */
    @Operation(summary = "重置管理员密码", description = "超级管理员重置指定管理员的后台登录密码")
    @PutMapping("/user/{id}/reset-pwd")
    @RequirePermission("sys:user:reset-pwd")
    public CommonResult<Void> resetAdminUserPassword(@PathVariable("id") Long id,
                                                     @RequestParam("password") String password) {
        boolean success = adminSysService.resetAdminUserPassword(id, password);
        if (success) {
            return CommonResult.success(null, "密码重置成功");
        }
        return CommonResult.failed("密码重置失败");
    }

    /**
     * 删除管理员账号
     */
    @Operation(summary = "删除管理员账号", description = "删除管理员，禁止删除系统初始超级管理员")
    @DeleteMapping("/user/{id}")
    @RequirePermission("sys:user:delete")
    public CommonResult<Void> deleteAdminUser(@PathVariable("id") Long id) {
        boolean success = adminSysService.deleteAdminUser(id);
        if (success) {
            return CommonResult.success(null, "管理员删除成功");
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 查询角色列表
     */
    @Operation(summary = "查询全量角色列表", description = "获取系统配置的所有角色及其拥有的权限标识数组")
    @GetMapping("/role/list")
    @RequirePermission("sys:role:view")
    public CommonResult<List<RoleVO>> listRoles() {
        List<RoleVO> list = adminSysService.listRoles();
        return CommonResult.success(list);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色", description = "定义角色名称、唯一编码与所关联的操作权限编码列表")
    @PostMapping("/role")
    @RequirePermission("sys:role:add")
    public CommonResult<RoleVO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        RoleVO vo = adminSysService.createRole(roleDTO);
        return CommonResult.success(vo, "角色创建成功");
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色", description = "修改角色的名称、描述与权限分配列表")
    @PutMapping("/role/{id}")
    @RequirePermission("sys:role:edit")
    public CommonResult<RoleVO> updateRole(@PathVariable("id") Long id,
                                           @Valid @RequestBody RoleDTO roleDTO) {
        RoleVO vo = adminSysService.updateRole(id, roleDTO);
        return CommonResult.success(vo, "角色修改成功");
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "若角色下有关联的在用管理员则禁止删除")
    @DeleteMapping("/role/{id}")
    @RequirePermission("sys:role:delete")
    public CommonResult<Void> deleteRole(@PathVariable("id") Long id) {
        boolean success = adminSysService.deleteRole(id);
        if (success) {
            return CommonResult.success(null, "角色删除成功");
        }
        return CommonResult.failed("删除失败");
    }
}
