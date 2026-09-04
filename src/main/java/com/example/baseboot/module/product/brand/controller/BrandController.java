package com.example.baseboot.module.product.brand.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.brand.dto.BrandCreateDTO;
import com.example.baseboot.module.product.brand.dto.BrandQueryDTO;
import com.example.baseboot.module.product.brand.dto.BrandUpdateDTO;
import com.example.baseboot.module.product.brand.service.BrandService;
import com.example.baseboot.module.product.brand.vo.BrandVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品品牌控制器
 */
@Tag(name = "07. 商品品牌管理 (BrandController)", description = "品牌创建、编辑、删除、详情查询、分页检索与状态启停")
@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * 创建商品品牌
     */
    @Operation(summary = "创建商品品牌", description = "录入新品牌并校验品牌名称唯一性")
    @PostMapping
    @LoginRequired
    public CommonResult<BrandVO> createBrand(@Valid @RequestBody BrandCreateDTO createDTO) {
        BrandVO brandVO = brandService.createBrand(createDTO);
        return CommonResult.success(brandVO, "商品品牌创建成功");
    }

    /**
     * 修改商品品牌
     */
    @Operation(summary = "修改商品品牌", description = "修改指定品牌的名称、Logo 图片、介绍与排序")
    @PutMapping("/{id}")
    @LoginRequired
    public CommonResult<BrandVO> updateBrand(@PathVariable("id") Long id,
                                             @Valid @RequestBody BrandUpdateDTO updateDTO) {
        BrandVO brandVO = brandService.updateBrand(id, updateDTO);
        return CommonResult.success(brandVO, "商品品牌修改成功");
    }

    /**
     * 删除商品品牌
     */
    @Operation(summary = "删除商品品牌", description = "若品牌下已有关联 SPU 商品则禁止删除")
    @DeleteMapping("/{id}")
    @LoginRequired
    public CommonResult<Void> deleteBrand(@PathVariable("id") Long id) {
        boolean success = brandService.deleteBrand(id);
        if (success) {
            return CommonResult.success(null, "商品品牌删除成功");
        }
        return CommonResult.failed("商品品牌删除失败");
    }

    /**
     * 根据ID获取品牌详情
     */
    @Operation(summary = "获取品牌详情", description = "按品牌 ID 查询单个品牌的详细信息")
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<BrandVO> getBrandById(@PathVariable("id") Long id) {
        BrandVO brandVO = brandService.getBrandById(id);
        return CommonResult.success(brandVO);
    }

    /**
     * 分页查询品牌列表
     */
    @Operation(summary = "分页查询品牌列表", description = "按品牌名称关键字与状态多条件筛选品牌")
    @GetMapping("/page")
    @PassToken
    public CommonResult<CommonPage<BrandVO>> pageBrands(BrandQueryDTO queryDTO) {
        CommonPage<BrandVO> page = brandService.pageBrands(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 获取所有启用的品牌列表 (下拉框使用)
     */
    @Operation(summary = "获取全量可用品牌列表", description = "返回所有启用状态的品牌下拉字典，用于商品发布联动")
    @GetMapping("/list-all")
    @PassToken
    public CommonResult<List<BrandVO>> listAllBrands() {
        List<BrandVO> list = brandService.listAllBrands();
        return CommonResult.success(list);
    }

    /**
     * 启用/禁用品牌
     */
    @Operation(summary = "启用/禁用品牌", description = "快速切换品牌的可用状态")
    @PutMapping("/{id}/status")
    @LoginRequired
    public CommonResult<Void> updateStatus(@PathVariable("id") Long id,
                                           @RequestParam("status") Integer status) {
        boolean success = brandService.updateStatus(id, status);
        if (success) {
            return CommonResult.success(null, "品牌状态更新成功");
        }
        return CommonResult.failed("品牌状态更新失败");
    }
}
