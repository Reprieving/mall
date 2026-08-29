package com.example.baseboot.module.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建管理员请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserCreateDTO {

    @NotBlank(message = "登录账号不能为空")
    private String username;

    @NotBlank(message = "初始密码不能为空")
    private String password;

    @NotBlank(message = "管理员昵称不能为空")
    private String nickname;

    private String avatar;
    private String email;
    private String phone;

    @NotNull(message = "必须分配所属角色")
    private Long roleId;

    private Integer status;
}
