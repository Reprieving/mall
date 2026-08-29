package com.example.baseboot.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页数据封装对象
 *
 * @param <T> 数据列表元素类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonPage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页数量
     */
    private Long pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long totalPages;

    /**
     * 分页数据列表
     */
    private List<T> list;

    /**
     * 将 MyBatis-Plus IPage 转换为通用分页结果
     */
    public static <T> CommonPage<T> restPage(IPage<T> pageInfo) {
        if (pageInfo == null) {
            return CommonPage.<T>builder()
                    .pageNum(1L)
                    .pageSize(10L)
                    .total(0L)
                    .totalPages(0L)
                    .list(Collections.emptyList())
                    .build();
        }
        return CommonPage.<T>builder()
                .pageNum(pageInfo.getCurrent())
                .pageSize(pageInfo.getSize())
                .total(pageInfo.getTotal())
                .totalPages(pageInfo.getPages())
                .list(pageInfo.getRecords())
                .build();
    }

    /**
     * 将 MyBatis-Plus IPage 配合转换后的 VO 列表转换为通用分页结果
     */
    public static <T, R> CommonPage<R> restPage(IPage<T> pageInfo, List<R> list) {
        if (pageInfo == null) {
            return CommonPage.<R>builder()
                    .pageNum(1L)
                    .pageSize(10L)
                    .total(0L)
                    .totalPages(0L)
                    .list(Collections.emptyList())
                    .build();
        }
        return CommonPage.<R>builder()
                .pageNum(pageInfo.getCurrent())
                .pageSize(pageInfo.getSize())
                .total(pageInfo.getTotal())
                .totalPages(pageInfo.getPages())
                .list(list != null ? list : Collections.emptyList())
                .build();
    }

    /**
     * 将普通 List 分页转换为通用分页结果
     */
    public static <T> CommonPage<T> restPage(List<T> list, long pageNum, long pageSize, long total) {
        long totalPages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
        return CommonPage.<T>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPages(totalPages)
                .list(list != null ? list : Collections.emptyList())
                .build();
    }
}
