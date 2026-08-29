package com.example.baseboot.module.product.spu.vo;

import com.example.baseboot.module.product.spu.entity.Spu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPU 商品简要视图对象 (用于列表展示)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long shopId;
    private String shopName;
    private String name;
    private String spuCode;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String title;
    private String description;
    private String mainPic;
    private String sliderPics;
    private Integer specType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalStock;
    private String unit;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static SpuVO fromEntity(Spu entity) {
        if (entity == null) {
            return null;
        }
        return SpuVO.builder()
                .id(entity.getId())
                .shopId(entity.getShopId())
                .name(entity.getName())
                .spuCode(entity.getSpuCode())
                .categoryId(entity.getCategoryId())
                .brandId(entity.getBrandId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .mainPic(entity.getMainPic())
                .sliderPics(entity.getSliderPics())
                .specType(entity.getSpecType())
                .minPrice(entity.getMinPrice())
                .maxPrice(entity.getMaxPrice())
                .totalStock(entity.getTotalStock())
                .unit(entity.getUnit())
                .status(entity.getStatus())
                .sort(entity.getSort())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
