package com.example.baseboot.module.system.vo;

import com.example.baseboot.module.system.entity.SysAdminUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员用户视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserVO implements Serializable {

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
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AdminUserVO fromEntity(SysAdminUser entity) {
        if (entity == null) {
            return null;
        }
        return AdminUserVO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .nickname(entity.getNickname())
                .avatar(entity.getAvatar())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .roleId(entity.getRoleId())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
