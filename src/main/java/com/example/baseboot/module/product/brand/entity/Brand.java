package com.example.baseboot.module.product.brand.entity;

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
 * 商品品牌实体类 (对应表 pms_brand)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_brand")
public class Brand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌Logo URL
     */
    private String logo;

    /**
     * 品牌描述
     */
    private String description;

    /**
     * 检索首字母 (大写 A-Z)
     */
    private String firstLetter;

    /**
     * 排序权重
     */
    private Integer sort;

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
