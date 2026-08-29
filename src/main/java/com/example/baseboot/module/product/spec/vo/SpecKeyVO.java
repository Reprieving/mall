package com.example.baseboot.module.product.spec.vo;

import com.example.baseboot.module.product.spec.entity.SpecKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 规格项视图对象 (包含该规格项下的规格值列表)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecKeyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 旗下的规格值列表
     */
    @Builder.Default
    private List<SpecValueVO> values = new ArrayList<>();

    public static SpecKeyVO fromEntity(SpecKey entity) {
        if (entity == null) {
            return null;
        }
        return SpecKeyVO.builder()
                .id(entity.getId())
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .sort(entity.getSort())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .values(new ArrayList<>())
                .build();
    }
}
