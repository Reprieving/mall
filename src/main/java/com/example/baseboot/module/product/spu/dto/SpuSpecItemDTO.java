package com.example.baseboot.module.product.spu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SPU 选用的规格项与规格值定义传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuSpecItemDTO {

    /**
     * 规格项ID (若已有规格项则传入)
     */
    private Long specKeyId;

    /**
     * 规格维度名称 (如: 机身颜色, 存储容量, 尺码)
     */
    @NotBlank(message = "规格维度名称不能为空")
    private String specName;

    /**
     * 选用的规格值ID列表 (可选)
     */
    private List<Long> specValueIds;

    /**
     * 规格可选值列表 (如: ["原色钛金属", "暗夜黑"])
     */
    private List<String> specValues;
}
