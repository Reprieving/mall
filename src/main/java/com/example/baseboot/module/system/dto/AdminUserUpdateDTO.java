package com.example.baseboot.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改管理员资料请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateDTO {

    @NotBlank(message = "管理员昵称不能为空")
    private String nickname;

    private String avatar;
    private String email;
    private String phone;

    @NotNull(message = "必须分配所属角色")
    private Long roleId;

    private Integer status;
}
