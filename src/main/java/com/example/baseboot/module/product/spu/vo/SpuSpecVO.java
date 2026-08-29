package com.example.baseboot.module.product.spu.vo;

import com.example.baseboot.module.product.spec.vo.SpecValueVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * SPU 选用的规格维度及其可选值视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuSpecVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格项ID
     */
    private Long specKeyId;

    /**
     * 规格项名称 (如: 机身颜色)
     */
    private String specName;

    /**
     * 该 SPU 选用的规格值列表
     */
    @Builder.Default
    private List<SpecValueVO> values = new ArrayList<>();
}
