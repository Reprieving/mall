package com.example.baseboot.module.user.cert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证审核列表分页查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertQueryDTO {

    /**
     * 认证类型: 1-个人实名认证, 2-个体工商户认证, 3-企业认证
     */
    private Integer certType;

    /**
     * 审核状态: 0-待审核, 1-审核通过, 2-审核驳回
     */
    private Integer status;

    /**
     * 真实姓名 / 法人姓名 / 企业名称 模糊搜索
     */
    private String keyword;

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    @Builder.Default
    private Long pageSize = 10L;
}
