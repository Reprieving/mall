package com.example.baseboot.module.product.sku.vo;

import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SKU 视图对象 (包含绑定的结构化规格列表)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long spuId;
    private String skuCode;
    private String name;
    private String pic;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer lockStock;
    private BigDecimal weight;
    private BigDecimal volume;
    private String specData;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 绑定的结构化规格键值列表
     */
    @Builder.Default
    private List<SkuSpecValueVO> specValues = new ArrayList<>();

    public static SkuVO fromEntity(Sku entity) {
        if (entity == null) {
            return null;
        }
        return SkuVO.builder()
                .id(entity.getId())
                .spuId(entity.getSpuId())
                .skuCode(entity.getSkuCode())
                .name(entity.getName())
                .pic(entity.getPic())
                .price(entity.getPrice())
                .originalPrice(entity.getOriginalPrice())
                .costPrice(entity.getCostPrice())
                .stock(entity.getStock())
                .lockStock(entity.getLockStock())
                .weight(entity.getWeight())
                .volume(entity.getVolume())
                .specData(entity.getSpecData())
                .status(entity.getStatus())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .specValues(new ArrayList<>())
                .build();
    }
}
