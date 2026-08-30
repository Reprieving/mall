package com.example.baseboot.module.user.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 运营端买家用户多条件分页查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserQueryDTO {

    /**
     * 关键字 (昵称/邮箱/手机号)
     */
    private String keyword;

    /**
     * 账号状态: 1-正常, 0-禁用
     */
    private Integer status;

    /**
     * 注册开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 注册截止时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 当前页码
     */
    @Builder.Default
    private Long pageNum = 1L;

    /**
     * 每页条数
     */
    @Builder.Default
    private Long pageSize = 10L;
}
