package com.example.baseboot.module.user.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 密码修改请求 DTO
 */
@Data
public class PasswordUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原密码
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度需在6-32位之间")
    private String newPassword;
}
