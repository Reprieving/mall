package com.example.baseboot.module.product.category.vo;

import com.example.baseboot.module.product.category.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String icon;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static CategoryVO fromEntity(Category entity) {
        if (entity == null) {
            return null;
        }
        return CategoryVO.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .level(entity.getLevel())
                .icon(entity.getIcon())
                .sort(entity.getSort())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
