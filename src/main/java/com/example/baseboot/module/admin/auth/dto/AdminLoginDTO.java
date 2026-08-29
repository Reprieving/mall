package com.example.baseboot.module.admin.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 运营管理员登录请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginDTO {

    /**
     * 登录账号
     */
    @NotBlank(message = "管理员账号不能为空")
    private String username;

    /**
     * 登录密码
     */
    @NotBlank(message = "登录密码不能为空")
    private String password;
}
