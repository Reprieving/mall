package com.example.baseboot.module.admin.system.vo;

import com.example.baseboot.module.admin.system.entity.SysRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 系统角色视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Long id;
    private String name;
    private String code;
    private String description;
    private List<String> permissions;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static RoleVO fromEntity(SysRole entity) {
        if (entity == null) {
            return null;
        }

        List<String> perms = Collections.emptyList();
        if (StringUtils.hasText(entity.getPermissions())) {
            try {
                perms = OBJECT_MAPPER.readValue(entity.getPermissions(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
                perms = Collections.singletonList(entity.getPermissions().trim());
            }
        }

        return RoleVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .permissions(perms)
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
