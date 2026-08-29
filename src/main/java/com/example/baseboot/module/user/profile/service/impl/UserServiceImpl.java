package com.example.baseboot.module.user.profile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.common.utils.PasswordUtils;
import com.example.baseboot.module.user.profile.dto.PasswordUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.entity.SysUser;
import com.example.baseboot.module.user.profile.mapper.SysUserMapper;
import com.example.baseboot.module.user.profile.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务实现类 (基于 MyBatis-Plus ServiceImpl)
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Override
    public UserVO getUserById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "用户ID不能为空");
        }
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        return UserVO.fromEntity(user);
    }

    @Override
    public SysUser getByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProfile(Long userId, UserUpdateDTO updateDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        SysUser existingUser = this.getById(userId);
        if (existingUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(StringUtils.hasText(updateDTO.getNickname()), SysUser::getNickname, updateDTO.getNickname())
                .set(StringUtils.hasText(updateDTO.getAvatar()), SysUser::getAvatar, updateDTO.getAvatar())
                .set(StringUtils.hasText(updateDTO.getPhone()), SysUser::getPhone, updateDTO.getPhone())
                .set(updateDTO.getGender() != null, SysUser::getGender, updateDTO.getGender())
                .set(StringUtils.hasText(updateDTO.getBio()), SysUser::getBio, updateDTO.getBio())
                .set(SysUser::getUpdateTime, LocalDateTime.now());

        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 校验原密码
        if (!PasswordUtils.matches(passwordUpdateDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "原密码不正确");
        }

        // 加密新密码并更新
        String encodedNewPassword = PasswordUtils.encode(passwordUpdateDTO.getNewPassword());
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, encodedNewPassword)
                .set(SysUser::getUpdateTime, LocalDateTime.now());

        return this.update(updateWrapper);
    }

    @Override
    public List<UserVO> listUsers() {
        List<SysUser> list = this.list(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getId));
        return list.stream()
                .map(UserVO::fromEntity)
                .collect(Collectors.toList());
    }
}
