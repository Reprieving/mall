package com.example.baseboot.module.user.profile.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.context.UserContext;
import com.example.baseboot.module.user.profile.dto.PasswordUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserUpdateDTO;
import com.example.baseboot.module.user.profile.dto.UserVO;
import com.example.baseboot.module.user.profile.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息管理控制器
 */
@Tag(name = "17. 买家个人中心 (UserController)", description = "买家端个人基本资料查询、编辑个人信息、修改密码")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@LoginRequired
public class UserController {

    private final UserService userService;

    /**
     * 查询当前登录用户的个人信息
     */
    @Operation(summary = "查询个人信息", description = "获取当前登录用户的头像、昵称、邮箱、手机号、性别与个人简介")
    @GetMapping("/info")
    public CommonResult<UserVO> getCurrentUserInfo() {
        Long currentUserId = UserContext.getUserId();
        UserVO userVO = userService.getUserById(currentUserId);
        return CommonResult.success(userVO);
    }

    /**
     * 修改当前登录用户的个人信息
     */
    @Operation(summary = "修改个人信息", description = "更新当前登录用户的昵称、头像、手机号、性别和个人简介")
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
    @Operation(summary = "修改密码", description = "输入原旧密码与新密码，验证原密码正确后加密更新")
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
    @Operation(summary = "按ID查询用户信息", description = "根据买家用户 ID 查询用户的公开基础资料")
    @GetMapping("/{id}")
    public CommonResult<UserVO> getUserById(@PathVariable("id") Long id) {
        UserVO userVO = userService.getUserById(id);
        return CommonResult.success(userVO);
    }

    /**
     * 查询用户列表
     */
    @Operation(summary = "查询用户列表", description = "查询所有注册买家用户列表")
    @GetMapping("/list")
    public CommonResult<List<UserVO>> listUsers() {
        List<UserVO> users = userService.listUsers();
        return CommonResult.success(users);
    }
}
