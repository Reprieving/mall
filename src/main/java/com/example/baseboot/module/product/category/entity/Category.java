package com.example.baseboot.module.product.category.entity;

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
 * 商品分类实体类 (对应表 pms_category)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父分类ID (0为顶级分类)
     */
    private Long parentId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 层级: 1-一级, 2-二级, 3-三级
     */
    private Integer level;

    /**
     * 分类图标URL
     */
    private String icon;

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
