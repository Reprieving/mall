package com.example.baseboot.module.user.profile.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.module.user.profile.dto.PasswordUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.entity.SysUser;

import java.util.List;

/**
 * 用户业务接口 (继承 MyBatis-Plus IService)
 */
public interface UserService extends IService<SysUser> {

    /**
     * 根据用户 ID 查询脱敏信息
     */
    UserVO getUserById(Long id);

    /**
     * 根据邮箱查询完整用户实体
     */
    SysUser getByEmail(String email);

    /**
     * 更新指定用户的个人资料
     */
    boolean updateProfile(Long userId, UserUpdateDTO updateDTO);

    /**
     * 修改用户密码
     */
    boolean updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO);

    /**
     * 获取所有用户列表
     */
    List<UserVO> listUsers();
}
