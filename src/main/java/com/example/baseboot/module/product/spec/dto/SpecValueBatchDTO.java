package com.example.baseboot.module.product.spec.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量创建规格值请求参数
 */
@Data
public class SpecValueBatchDTO {

    /**
     * 所属规格项ID
     */
    @NotNull(message = "规格项ID不能为空")
    private Long specKeyId;

    /**
     * 规格值列表
     */
    @NotEmpty(message = "规格值列表不能为空")
    private List<String> values;
}
