package com.example.baseboot.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.dto.SpuAdminQueryDTO;
import com.example.baseboot.module.product.dto.SpuBatchCategoryDTO;
import com.example.baseboot.module.product.dto.SpuBatchDeleteDTO;
import com.example.baseboot.module.product.vo.StockWarningVO;
import com.example.baseboot.module.product.brand.entity.Brand;
import com.example.baseboot.module.product.brand.mapper.BrandMapper;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.mapper.CategoryMapper;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.product.spu.service.SpuService;
import com.example.baseboot.module.product.spu.vo.SpuVO;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 运营端商品检索与预警控制器
 */
@Tag(name = "商品运营管理 (AdminProductController)", description = "商品检索、批量下架、类目转移与库存预警")
@RestController
@RequestMapping("/api/admin/spu")
@RequiredArgsConstructor
public class AdminProductController {

    private final SpuService spuService;
    private final SpuMapper spuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final ShopMapper shopMapper;

    /**
     * 全平台商品跨店铺多条件高级检索
     */
    @Operation(summary = "全平台跨店铺商品高级检索", description = "综合关键字、SPU 编码、所属店铺、类目、品牌、状态与库存阈值等多维组合检索")
    @GetMapping("/page")
    @RequirePermission("spu:view")
    public CommonResult<CommonPage<SpuVO>> pageSpu(SpuAdminQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new SpuAdminQueryDTO();
        }

        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Spu> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(Spu::getName, kw).or().like(Spu::getTitle, kw));
        }
        if (StringUtils.hasText(queryDTO.getSpuCode())) {
            wrapper.eq(Spu::getSpuCode, queryDTO.getSpuCode().trim());
        }
        if (queryDTO.getShopId() != null) {
            wrapper.eq(Spu::getShopId, queryDTO.getShopId());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Spu::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getBrandId() != null) {
            wrapper.eq(Spu::getBrandId, queryDTO.getBrandId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Spu::getStatus, queryDTO.getStatus());
        }
        if (Boolean.TRUE.equals(queryDTO.getLowStock())) {
            int threshold = queryDTO.getLowStockThreshold() != null ? queryDTO.getLowStockThreshold() : 10;
            wrapper.le(Spu::getTotalStock, threshold);
        }

        wrapper.orderByAsc(Spu::getSort).orderByDesc(Spu::getId);
        Page<Spu> spuPage = spuMapper.selectPage(page, wrapper);

        if (CollectionUtils.isEmpty(spuPage.getRecords())) {
            return CommonResult.success(CommonPage.restPage(spuPage, Collections.emptyList()));
        }

        Set<Long> shopIds = spuPage.getRecords().stream().map(Spu::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = spuPage.getRecords().stream().map(Spu::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> brandIds = spuPage.getRecords().stream().map(Spu::getBrandId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> shopNameMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            if (shops != null) {
                shopNameMap = shops.stream().collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));
            }
        }

        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            if (categories != null) {
                categoryNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
            }
        }

        Map<Long, String> brandNameMap = new HashMap<>();
        if (!brandIds.isEmpty()) {
            List<Brand> brands = brandMapper.selectBatchIds(brandIds);
            if (brands != null) {
                brandNameMap = brands.stream().collect(Collectors.toMap(Brand::getId, Brand::getName, (a, b) -> a));
            }
        }

        Map<Long, String> finalShopNameMap = shopNameMap;
        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        Map<Long, String> finalBrandNameMap = brandNameMap;

        List<SpuVO> voList = spuPage.getRecords().stream().map(s -> {
            SpuVO vo = SpuVO.fromEntity(s);
            vo.setShopName(finalShopNameMap.get(s.getShopId()));
            vo.setCategoryName(finalCategoryNameMap.get(s.getCategoryId()));
            vo.setBrandName(finalBrandNameMap.get(s.getBrandId()));
            return vo;
        }).collect(Collectors.toList());

        return CommonResult.success(CommonPage.restPage(spuPage, voList));
    }

    /**
     * 批量下架 / 删除违规商品
     */
    @Operation(summary = "批量删除违规商品", description = "平台运营一键批量删除或强制清理违规违法商品")
    @DeleteMapping("/batch")
    @RequirePermission("spu:delete")
    public CommonResult<Integer> batchDeleteSpu(@Valid @RequestBody SpuBatchDeleteDTO batchDTO) {
        int count = 0;
        for (Long id : batchDTO.getIds()) {
            try {
                boolean ok = spuService.deleteSpu(id);
                if (ok) {
                    count++;
                }
            } catch (Exception ignored) {
            }
        }
        return CommonResult.success(count, "批量删除完成，成功删除 " + count + " 件商品");
    }

    /**
     * 批量转移商品分类
     */
    @Operation(summary = "批量变更商品类目", description = "在后台调整类目结构时，将勾选的多个商品快速迁移至指定新类目")
    @PutMapping("/batch/category")
    @RequirePermission("spu:edit")
    public CommonResult<Integer> batchUpdateCategory(@Valid @RequestBody SpuBatchCategoryDTO batchDTO) {
        Category category = categoryMapper.selectById(batchDTO.getTargetCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        int count = 0;
        for (Long id : batchDTO.getIds()) {
            Spu spu = spuMapper.selectById(id);
            if (spu != null) {
                spu.setCategoryId(batchDTO.getTargetCategoryId());
                spu.setUpdateTime(LocalDateTime.now());
                spuMapper.updateById(spu);
                count++;
            }
        }
        return CommonResult.success(count, "批量修改分类完成，成功更新 " + count + " 件商品");
    }

    /**
     * 查询库存告急预警列表 (按库存升序)
     */
    @Operation(summary = "库存告急预警大盘", description = "筛选总库存低于告急警戒阈值（默认 10 件）的在售商品，按库存升序排查")
    @GetMapping("/stock-warning")
    @RequirePermission("spu:view")
    public CommonResult<List<StockWarningVO>> getStockWarningList(@RequestParam(value = "threshold", defaultValue = "10") Integer threshold,
                                                                 @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        List<Spu> lowStockSpus = spuMapper.selectList(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getStatus, 1)
                .le(Spu::getTotalStock, threshold)
                .orderByAsc(Spu::getTotalStock)
                .last("LIMIT " + limit));

        if (CollectionUtils.isEmpty(lowStockSpus)) {
            return CommonResult.success(Collections.emptyList());
        }

        Set<Long> shopIds = lowStockSpus.stream().map(Spu::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = lowStockSpus.stream().map(Spu::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> shopNameMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            if (shops != null) {
                shopNameMap = shops.stream().collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));
            }
        }

        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            if (categories != null) {
                categoryNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
            }
        }

        Map<Long, String> finalShopNameMap = shopNameMap;
        Map<Long, String> finalCategoryNameMap = categoryNameMap;

        List<StockWarningVO> list = lowStockSpus.stream().map(s -> StockWarningVO.builder()
                .spuId(s.getId())
                .spuName(s.getName())
                .spuCode(s.getSpuCode())
                .spuPic(s.getMainPic())
                .shopId(s.getShopId())
                .shopName(finalShopNameMap.get(s.getShopId()))
                .categoryId(s.getCategoryId())
                .categoryName(finalCategoryNameMap.get(s.getCategoryId()))
                .totalStock(s.getTotalStock())
                .minPrice(s.getMinPrice())
                .maxPrice(s.getMaxPrice())
                .status(s.getStatus())
                .build()).collect(Collectors.toList());

        return CommonResult.success(list);
    }
}
