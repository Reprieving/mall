package com.example.baseboot.module.product.brand.vo;

import com.example.baseboot.module.product.brand.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品品牌视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String logo;
    private String description;
    private String firstLetter;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static BrandVO fromEntity(Brand entity) {
        if (entity == null) {
            return null;
        }
        return BrandVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .logo(entity.getLogo())
                .description(entity.getDescription())
                .firstLetter(entity.getFirstLetter())
                .sort(entity.getSort())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
