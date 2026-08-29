package com.example.baseboot.module.product.spu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量更新状态请求参数
 */
@Data
public class BatchStatusDTO {

    /**
     * 主键ID列表
     */
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    /**
     * 目标状态: 0-下架/禁用, 1-上架/启用
     */
    @NotNull(message = "状态值不能为空")
    private Integer status;
}
