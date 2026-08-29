package com.example.baseboot.module.product.spec.vo;

import com.example.baseboot.module.product.spec.entity.SpecValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 规格值视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecValueVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long specKeyId;
    private String value;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static SpecValueVO fromEntity(SpecValue entity) {
        if (entity == null) {
            return null;
        }
        return SpecValueVO.builder()
                .id(entity.getId())
                .specKeyId(entity.getSpecKeyId())
                .value(entity.getValue())
                .sort(entity.getSort())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
