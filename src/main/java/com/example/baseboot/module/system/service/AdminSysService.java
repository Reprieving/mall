package com.example.baseboot.module.system.service;

import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.system.dto.AdminUserCreateDTO;
import com.example.baseboot.module.system.dto.AdminUserUpdateDTO;
import com.example.baseboot.module.system.dto.RoleDTO;
import com.example.baseboot.module.system.vo.AdminUserVO;
import com.example.baseboot.module.system.vo.RoleVO;

import java.util.List;

/**
 * 运营端 RBAC 角色与管理员管理服务
 */
public interface AdminSysService {

    // 管理员管理
    CommonPage<AdminUserVO> pageAdminUsers(Long pageNum, Long pageSize, String keyword, Long roleId, Integer status);
    AdminUserVO createAdminUser(AdminUserCreateDTO createDTO);
    AdminUserVO updateAdminUser(Long id, AdminUserUpdateDTO updateDTO);
    boolean updateAdminUserStatus(Long id, Integer status);
    boolean resetAdminUserPassword(Long id, String newPassword);
    boolean deleteAdminUser(Long id);

    // 角色管理
    List<RoleVO> listRoles();
    RoleVO createRole(RoleDTO roleDTO);
    RoleVO updateRole(Long id, RoleDTO roleDTO);
    boolean deleteRole(Long id);
}
