package com.example.baseboot.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 运营管理员当前登录信息与权限清单视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Long roleId;
    private String roleName;
    private String roleCode;
    private List<String> permissions;
}
