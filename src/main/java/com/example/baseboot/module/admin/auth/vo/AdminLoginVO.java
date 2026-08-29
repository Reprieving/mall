package com.example.baseboot.module.admin.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 运营管理员登录成功视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String roleName;
    private String roleCode;
    private String token;
    private String tokenPrefix;
    private Long expiresIn;
    private List<String> permissions;
}
