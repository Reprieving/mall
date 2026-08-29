package com.example.baseboot.module.user.profile.dto;

import com.example.baseboot.module.user.profile.entity.SysUser;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息展示脱敏对象 (VO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer gender;
    private String bio;
    private Integer status;
    private Integer certStatus;
    private String realName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 从 Entity 转为 VO
     */
    public static UserVO fromEntity(SysUser user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender())
                .bio(user.getBio())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
