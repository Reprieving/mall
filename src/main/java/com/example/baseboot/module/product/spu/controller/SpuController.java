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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 SPU 控制器
 */
@RestController
@RequestMapping("/api/spu")
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;

    /**
     * 创建 SPU 商品 (支持多规格定义与 SKU 矩阵一体化录入)
     */
    @PostMapping
    @LoginRequired
    public CommonResult<SpuDetailVO> createSpu(@Valid @RequestBody SpuCreateDTO createDTO) {
        SpuDetailVO detailVO = spuService.createSpu(createDTO);
        return CommonResult.success(detailVO, "商品创建成功");
    }

    /**
     * 修改 SPU 商品信息 (含规格与 SKU 列表维护)
     */
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
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<SpuDetailVO> getSpuDetail(@PathVariable("id") Long id) {
        SpuDetailVO detailVO = spuService.getSpuDetail(id);
        return CommonResult.success(detailVO);
    }

    /**
     * 分页查询 SPU 商品列表 (支持多条件组合查询)
     */
    @GetMapping("/page")
    @PassToken
    public CommonResult<CommonPage<SpuVO>> pageSpu(SpuQueryDTO queryDTO) {
        CommonPage<SpuVO> page = spuService.pageSpu(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 修改单个商品上下架状态
     */
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
