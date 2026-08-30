package com.example.baseboot.module.product.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 运营端批量删除商品参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuBatchDeleteDTO {

    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> ids;
}
