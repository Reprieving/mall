package com.example.baseboot.module.product.sku.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品 SKU 实体类 (对应表 pms_sku)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_sku")
public class Sku implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属 SPU ID
     */
    private Long spuId;

    /**
     * SKU 编码/条形码
     */
    private String skuCode;

    /**
     * SKU 名称/描述
     */
    private String name;

    /**
     * SKU 专属图片
     */
    private String pic;

    /**
     * 销售价格
     */
    private BigDecimal price;

    /**
     * 市场划线价
     */
    private BigDecimal originalPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 当前库存
     */
    private Integer stock;

    /**
     * 锁定/预占库存
     */
    private Integer lockStock;

    /**
     * 重量 (kg)
     */
    private BigDecimal weight;

    /**
     * 体积 (m³)
     */
    private BigDecimal volume;

    /**
     * 多规格属性键值对组合 JSON
     * 格式示例: [{"specName":"机身颜色","specValue":"星宇黑"},{"specName":"存储容量","specValue":"128GB"}]
     */
    private String specData;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
