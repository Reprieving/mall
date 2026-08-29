package com.example.baseboot.module.admin.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色新增/修改请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    @NotBlank(message = "角色名称不能为空")
    private String name;

    @NotBlank(message = "角色标识编码不能为空")
    private String code;

    private String description;

    /**
     * 权限标识列表 (如: ["order:deliver", "shop:audit"])
     */
    private List<String> permissions;

    private Integer status;
}
