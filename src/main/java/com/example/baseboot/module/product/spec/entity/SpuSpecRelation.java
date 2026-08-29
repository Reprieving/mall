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
 * SPU 规格选用关联实体 (对应表 pms_spu_spec_relation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("pms_spu_spec_relation")
public class SpuSpecRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 规格项ID
     */
    private Long specKeyId;

    /**
     * 规格值ID
     */
    private Long specValueId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
