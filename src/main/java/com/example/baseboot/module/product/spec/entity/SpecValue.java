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
 * 商品规格值实体 (对应表 pms_spec_value)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_spec_value")
public class SpecValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格值ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属规格项ID
     */
    private Long specKeyId;

    /**
     * 规格值 (如: 原色钛金属, 暗夜黑, 256GB, XXL)
     */
    private String value;

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
