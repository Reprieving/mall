package com.example.baseboot.module.product.spu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.annotation.RequirePermission;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.spu.dto.BatchStatusDTO;
import com.example.baseboot.module.product.spu.dto.SpuAdminQueryDTO;
import com.example.baseboot.module.product.spu.dto.SpuBatchCategoryDTO;
import com.example.baseboot.module.product.spu.dto.SpuBatchDeleteDTO;
import com.example.baseboot.module.product.spu.dto.SpuCreateDTO;
import com.example.baseboot.module.product.spu.dto.SpuUpdateDTO;
import com.example.baseboot.module.product.spu.vo.SpuDetailVO;
import com.example.baseboot.module.product.spu.vo.StockWarningVO;
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
@RequestMapping("/admin/spu")
@RequiredArgsConstructor
public class AdminProductController {

    private final SpuService spuService;
    private final SpuMapper spuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final ShopMapper shopMapper;

    /**
     * 创建 SPU 商品 (支持多规格定义与 SKU 矩阵一体化录入)
     */
    @Operation(summary = "创建 SPU 商品", description = "创建新商品，支持定义规格项列表并一并初始化 SKU 规格定价矩阵")
    @PostMapping
    @LoginRequired
    public CommonResult<SpuDetailVO> createSpu(@Valid @RequestBody SpuCreateDTO createDTO) {
        SpuDetailVO detailVO = spuService.createSpu(createDTO);
        return CommonResult.success(detailVO, "商品创建成功");
    }

    /**
     * 修改 SPU 商品信息 (含规格与 SKU 列表维护)
     */
    @Operation(summary = "修改 SPU 商品", description = "修改商品主图、轮播图、富文本详情、类目、品牌以及全量 SKU 矩阵")
    @PutMapping("/{id}")
    @LoginRequired
    public CommonResult<SpuDetailVO> updateSpu(@PathVariable("id") Long id,
                                               @Valid @RequestBody SpuUpdateDTO updateDTO) {
        SpuDetailVO detailVO = spuService.updateSpu(id, updateDTO);
        return CommonResult.success(detailVO, "商品信息更新成功");
    }

    /**
     * 删除 SPU 商品 (级联清理旗下所有规格定义与 SKU)
     */
    @Operation(summary = "删除 SPU 商品", description = "删除商品 SPU 并级联清理旗下所绑定的规格和所有 SKU")
    @DeleteMapping("/{id}")
    @LoginRequired
    public CommonResult<Void> deleteSpu(@PathVariable("id") Long id) {
        boolean success = spuService.deleteSpu(id);
        if (success) {
            return CommonResult.success(null, "商品删除成功");
        }
        return CommonResult.failed("商品删除失败");
    }

    /**
     * 查询 SPU 完整详情 (基础信息 + 分类 + 品牌 + 规格项定义 + 所有 SKU)
     */
    @Operation(summary = "查询 SPU 完整详情", description = "商品商详页与编辑页使用，获取 SPU 基础信息、分类名称、品牌信息、规格项定义及全量 SKU 列表")
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<SpuDetailVO> getSpuDetail(@PathVariable("id") Long id) {
        SpuDetailVO detailVO = spuService.getSpuDetail(id);
        return CommonResult.success(detailVO);
    }

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

    /**
     * 修改单个商品上下架状态
     */
    @Operation(summary = "修改单个商品上下架状态", description = "快速控制单件商品上架 (1) 或下架 (0)")
    @PutMapping("/{id}/status")
    @LoginRequired
    public CommonResult<Void> updateStatus(@PathVariable("id") Long id,
                                           @RequestParam("status") Integer status) {
        boolean success = spuService.updateStatus(id, status);
        if (success) {
            return CommonResult.success(null, "商品上下架状态更新成功");
        }
        return CommonResult.failed("商品状态更新失败");
    }

    /**
     * 批量修改商品上下架状态
     */
    @Operation(summary = "批量修改商品上下架状态", description = "批量将勾选的多个商品 SPU 进行统一上架或下架操作")
    @PutMapping("/batch/status")
    @LoginRequired
    public CommonResult<Void> batchUpdateStatus(@Valid @RequestBody BatchStatusDTO batchStatusDTO) {
        boolean success = spuService.batchUpdateStatus(batchStatusDTO.getIds(), batchStatusDTO.getStatus());
        if (success) {
            return CommonResult.success(null, "批量上下架状态更新成功");
        }
        return CommonResult.failed("批量更新状态失败");
    }
}
