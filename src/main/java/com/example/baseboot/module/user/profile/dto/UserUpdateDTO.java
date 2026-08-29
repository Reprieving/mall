package com.example.baseboot.module.user.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息修改请求 DTO
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像链接长度不能超过255个字符")
    private String avatar;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 性别: 0-保密, 1-男, 2-女
     */
    private Integer gender;

    /**
     * 个人简介/个性签名
     */
    @Size(max = 255, message = "个人简介长度不能超过255个字符")
    private String bio;
}
