package com.example.baseboot.module.product.spu.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.spu.dto.BatchStatusDTO;
import com.example.baseboot.module.product.spu.dto.SpuCreateDTO;
import com.example.baseboot.module.product.spu.dto.SpuQueryDTO;
import com.example.baseboot.module.product.spu.dto.SpuUpdateDTO;
import com.example.baseboot.module.product.spu.service.SpuService;
import com.example.baseboot.module.product.spu.vo.SpuDetailVO;
import com.example.baseboot.module.product.spu.vo.SpuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 SPU 控制器
 */
@Tag(name = "11. 商品SPU管控 (SpuController)", description = "商品 SPU 录入、矩阵生成、全量详情、多维检索与上下架控制")
@RestController
@RequestMapping("/api/spu")
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;

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
     * 分页查询 SPU 商品列表 (支持多条件组合查询)
     */
    @Operation(summary = "分页查询 SPU 列表", description = "多条件组合检索 SPU 列表，支持按关键字、类目、品牌、状态及价格范围筛选")
    @GetMapping("/page")
    @PassToken
    public CommonResult<CommonPage<SpuVO>> pageSpu(SpuQueryDTO queryDTO) {
        CommonPage<SpuVO> page = spuService.pageSpu(queryDTO);
        return CommonResult.success(page);
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
