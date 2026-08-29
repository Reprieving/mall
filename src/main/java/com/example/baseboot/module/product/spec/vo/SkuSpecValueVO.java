package com.example.baseboot.module.product.spec.vo;

import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SKU 绑定的具体规格键值视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuSpecValueVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格项ID
     */
    private Long specKeyId;

    /**
     * 规格项名称 (如: 机身颜色)
     */
    private String specKeyName;

    /**
     * 规格值ID
     */
    private Long specValueId;

    /**
     * 规格值内容 (如: 原色钛金属)
     */
    private String specValue;

    public static SkuSpecValueVO fromEntity(SkuSpecValue entity) {
        if (entity == null) {
            return null;
        }
        return SkuSpecValueVO.builder()
                .specKeyId(entity.getSpecKeyId())
                .specKeyName(entity.getSpecKeyName())
                .specValueId(entity.getSpecValueId())
                .specValue(entity.getSpecValue())
                .build();
    }
}
