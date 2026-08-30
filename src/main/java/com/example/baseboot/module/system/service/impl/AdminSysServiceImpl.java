package com.example.baseboot.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.common.utils.PasswordUtils;
import com.example.baseboot.module.system.dto.AdminUserCreateDTO;
import com.example.baseboot.module.system.dto.AdminUserUpdateDTO;
import com.example.baseboot.module.system.dto.RoleDTO;
import com.example.baseboot.module.system.entity.SysAdminUser;
import com.example.baseboot.module.system.entity.SysRole;
import com.example.baseboot.module.system.mapper.SysAdminUserMapper;
import com.example.baseboot.module.system.mapper.SysRoleMapper;
import com.example.baseboot.module.system.service.AdminSysService;
import com.example.baseboot.module.system.vo.AdminUserVO;
import com.example.baseboot.module.system.vo.RoleVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营端 RBAC 角色与管理员管理实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSysServiceImpl implements AdminSysService {

    private final SysAdminUserMapper adminUserMapper;
    private final SysRoleMapper roleMapper;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public CommonPage<AdminUserVO> pageAdminUsers(Long pageNum, Long pageSize, String keyword, Long roleId, Integer status) {
        long pNum = pageNum != null && pageNum > 0 ? pageNum : 1L;
        long pSize = pageSize != null && pageSize > 0 ? pageSize : 10L;

        Page<SysAdminUser> page = new Page<>(pNum, pSize);
        LambdaQueryWrapper<SysAdminUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysAdminUser::getUsername, kw)
                    .or().like(SysAdminUser::getNickname, kw)
                    .or().like(SysAdminUser::getPhone, kw));
        }
        if (roleId != null) {
            wrapper.eq(SysAdminUser::getRoleId, roleId);
        }
        if (status != null) {
            wrapper.eq(SysAdminUser::getStatus, status);
        }

        wrapper.orderByDesc(SysAdminUser::getId);
        Page<SysAdminUser> adminPage = adminUserMapper.selectPage(page, wrapper);

        if (CollectionUtils.isEmpty(adminPage.getRecords())) {
            return CommonPage.restPage(adminPage, Collections.emptyList());
        }

        Set<Long> roleIds = adminPage.getRecords().stream().map(SysAdminUser::getRoleId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysRole> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            if (roles != null) {
                roleMap = roles.stream().collect(Collectors.toMap(SysRole::getId, r -> r, (a, b) -> a));
            }
        }

        Map<Long, SysRole> finalRoleMap = roleMap;
        List<AdminUserVO> voList = adminPage.getRecords().stream().map(u -> {
            AdminUserVO vo = AdminUserVO.fromEntity(u);
            SysRole r = finalRoleMap.get(u.getRoleId());
            if (r != null) {
                vo.setRoleName(r.getName());
                vo.setRoleCode(r.getCode());
            }
            return vo;
        }).collect(Collectors.toList());

        return CommonPage.restPage(adminPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO createAdminUser(AdminUserCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "参数不能为空");
        }

        String username = createDTO.getUsername().trim();
        Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<SysAdminUser>().eq(SysAdminUser::getUsername, username));
        if (count > 0) {
            throw new BusinessException(ResultCode.ADMIN_USERNAME_EXISTS);
        }

        SysRole role = roleMapper.selectById(createDTO.getRoleId());
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_EXIST);
        }

        SysAdminUser admin = SysAdminUser.builder()
                .username(username)
                .password(PasswordUtils.encode(createDTO.getPassword()))
                .nickname(createDTO.getNickname().trim())
                .avatar(createDTO.getAvatar())
                .email(createDTO.getEmail())
                .phone(createDTO.getPhone())
                .roleId(createDTO.getRoleId())
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        adminUserMapper.insert(admin);

        AdminUserVO vo = AdminUserVO.fromEntity(admin);
        vo.setRoleName(role.getName());
        vo.setRoleCode(role.getCode());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO updateAdminUser(Long id, AdminUserUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "管理员ID不能为空");
        }
        SysAdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_EXIST);
        }

        SysRole role = roleMapper.selectById(updateDTO.getRoleId());
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_EXIST);
        }

        admin.setNickname(updateDTO.getNickname().trim());
        admin.setAvatar(updateDTO.getAvatar());
        admin.setEmail(updateDTO.getEmail());
        admin.setPhone(updateDTO.getPhone());
        admin.setRoleId(updateDTO.getRoleId());
        if (updateDTO.getStatus() != null) {
            admin.setStatus(updateDTO.getStatus());
        }
        admin.setUpdateTime(LocalDateTime.now());

        adminUserMapper.updateById(admin);

        AdminUserVO vo = AdminUserVO.fromEntity(admin);
        vo.setRoleName(role.getName());
        vo.setRoleCode(role.getCode());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdminUserStatus(Long id, Integer status) {
        if (id == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "参数不合法");
        }
        SysAdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_EXIST);
        }
        admin.setStatus(status);
        admin.setUpdateTime(LocalDateTime.now());
        return adminUserMapper.updateById(admin) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetAdminUserPassword(Long id, String newPassword) {
        if (id == null || !StringUtils.hasText(newPassword)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "新密码不能为空");
        }
        SysAdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(ResultCode.ADMIN_NOT_EXIST);
        }
        admin.setPassword(PasswordUtils.encode(newPassword.trim()));
        admin.setUpdateTime(LocalDateTime.now());
        return adminUserMapper.updateById(admin) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAdminUser(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "管理员ID不能为空");
        }
        if (id == 1L) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "初始超级管理员账号禁止删除");
        }
        return adminUserMapper.deleteById(id) > 0;
    }

    @Override
    public List<RoleVO> listRoles() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream().map(RoleVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(RoleDTO roleDTO) {
        if (roleDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "角色参数不能为空");
        }

        String code = roleDTO.getCode().trim().toUpperCase();
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code));
        if (count > 0) {
            throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
        }

        String permJson = null;
        if (!CollectionUtils.isEmpty(roleDTO.getPermissions())) {
            try {
                permJson = OBJECT_MAPPER.writeValueAsString(roleDTO.getPermissions());
            } catch (Exception ignored) {
            }
        }

        SysRole role = SysRole.builder()
                .name(roleDTO.getName().trim())
                .code(code)
                .description(roleDTO.getDescription())
                .permissions(permJson)
                .status(roleDTO.getStatus() != null ? roleDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        roleMapper.insert(role);
        return RoleVO.fromEntity(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVO updateRole(Long id, RoleDTO roleDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "角色ID不能为空");
        }
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.ROLE_NOT_EXIST);
        }

        String code = roleDTO.getCode().trim().toUpperCase();
        if (!code.equalsIgnoreCase(role.getCode())) {
            Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getCode, code)
                    .ne(SysRole::getId, id));
            if (count > 0) {
                throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
            }
            role.setCode(code);
        }

        String permJson = null;
        if (!CollectionUtils.isEmpty(roleDTO.getPermissions())) {
            try {
                permJson = OBJECT_MAPPER.writeValueAsString(roleDTO.getPermissions());
            } catch (Exception ignored) {
            }
        }

        role.setName(roleDTO.getName().trim());
        role.setDescription(roleDTO.getDescription());
        role.setPermissions(permJson);
        if (roleDTO.getStatus() != null) {
            role.setStatus(roleDTO.getStatus());
        }
        role.setUpdateTime(LocalDateTime.now());

        roleMapper.updateById(role);
        return RoleVO.fromEntity(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "角色ID不能为空");
        }
        if (id == 1L) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "超级管理员角色禁止删除");
        }
        Long userCount = adminUserMapper.selectCount(new LambdaQueryWrapper<SysAdminUser>().eq(SysAdminUser::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException(ResultCode.ROLE_IN_USE);
        }
        return roleMapper.deleteById(id) > 0;
    }
}
