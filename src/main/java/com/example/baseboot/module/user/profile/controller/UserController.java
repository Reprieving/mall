package com.example.baseboot.module.user.profile.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.user.profile.dto.PasswordUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息管理控制器
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@LoginRequired
public class UserController {

    private final UserService userService;

    /**
     * 查询当前登录用户的个人信息
     */
    @GetMapping("/info")
    public CommonResult<UserVO> getCurrentUserInfo() {
        Long currentUserId = UserContext.getUserId();
        UserVO userVO = userService.getUserById(currentUserId);
        return CommonResult.success(userVO);
    }

    /**
     * 修改当前登录用户的个人信息
     */
    @PutMapping("/update")
    public CommonResult<UserVO> updateProfile(@Valid @RequestBody UserUpdateDTO updateDTO) {
        Long currentUserId = UserContext.getUserId();
        boolean updated = userService.updateProfile(currentUserId, updateDTO);
        if (updated) {
            UserVO updatedUser = userService.getUserById(currentUserId);
            return CommonResult.success(updatedUser, "个人资料更新成功");
        }
        return CommonResult.failed("更新失败");
    }

    /**
     * 修改当前登录用户的密码
     */
    @PutMapping("/update-password")
    public CommonResult<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        Long currentUserId = UserContext.getUserId();
        boolean updated = userService.updatePassword(currentUserId, passwordUpdateDTO);
        if (updated) {
            return CommonResult.success(null, "密码修改成功");
        }
        return CommonResult.failed("密码修改失败");
    }

    /**
     * 根据用户 ID 查询用户信息
     */
    @GetMapping("/{id}")
    public CommonResult<UserVO> getUserById(@PathVariable("id") Long id) {
        UserVO userVO = userService.getUserById(id);
        return CommonResult.success(userVO);
    }

    /**
     * 查询用户列表
     */
    @GetMapping("/list")
    public CommonResult<List<UserVO>> listUsers() {
        List<UserVO> users = userService.listUsers();
        return CommonResult.success(users);
    }
}
