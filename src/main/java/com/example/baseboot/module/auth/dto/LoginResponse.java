package com.example.baseboot.module.auth.dto;

import com.example.baseboot.module.user.profile.dto.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录成功返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问 Token
     */
    private String token;

    /**
     * Token 类型 (如 Bearer)
     */
    private String tokenType;

    /**
     * 过期时间 (秒)
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserVO userInfo;
}
