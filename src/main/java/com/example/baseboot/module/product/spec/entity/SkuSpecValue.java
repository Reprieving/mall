package com.example.baseboot.module.product.spec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SKU 规格绑定关系实体 (对应表 pms_sku_spec_value)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_sku_spec_value")
public class SkuSpecValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 规格项ID
     */
    private Long specKeyId;

    /**
     * 规格项名称 (冗余快照)
     */
    private String specKeyName;

    /**
     * 规格值ID
     */
    private Long specValueId;

    /**
     * 规格值 (冗余快照)
     */
    private String specValue;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
