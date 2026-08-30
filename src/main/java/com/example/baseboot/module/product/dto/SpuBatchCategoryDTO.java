package com.example.baseboot.module.product.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运营端批量调整商品分类参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuBatchCategoryDTO {

    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> ids;

    @NotNull(message = "目标分类ID不能为空")
    private Long targetCategoryId;
}
