package com.example.baseboot.module.product.sku.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
import com.example.baseboot.module.product.sku.dto.SkuPriceUpdateDTO;
import com.example.baseboot.module.product.sku.dto.SkuStockUpdateDTO;
import com.example.baseboot.module.product.sku.dto.SkuUpdateDTO;
import com.example.baseboot.module.product.sku.service.SkuService;
import com.example.baseboot.module.product.sku.vo.SkuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 SKU 控制器
 */
@Tag(name = "09. 商品规格库存SKU (SkuController)", description = "商品 SKU 矩阵录入、更新、独立调价、库存调整与上下架")
@RestController
@RequestMapping("/api/sku")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    /**
     * 为指定 SPU 新增单个 SKU (自动联动汇总 SPU 价格与总库存)
     */
    @Operation(summary = "为 SPU 新增单个 SKU", description = "在已有 SPU 下追加具体规格规格组合的 SKU 单品，自动同步该 SPU 的价格区间与总库存")
    @PostMapping("/spu/{spuId}")
    @LoginRequired
    public CommonResult<SkuVO> createSku(@PathVariable("spuId") Long spuId,
                                         @Valid @RequestBody SkuItemDTO skuDTO) {
        SkuVO skuVO = skuService.createSku(spuId, skuDTO);
        return CommonResult.success(skuVO, "SKU新增成功");
    }

    /**
     * 修改单个 SKU 信息 (价格、原价、成本价、图片、规格等)
     */
    @Operation(summary = "修改单个 SKU 信息", description = "更新指定 SKU 的售价、原价、成本价、单品缩略图及条码")
    @PutMapping("/{id}")
    @LoginRequired
    public CommonResult<SkuVO> updateSku(@PathVariable("id") Long id,
                                         @Valid @RequestBody SkuUpdateDTO updateDTO) {
        SkuVO skuVO = skuService.updateSku(id, updateDTO);
        return CommonResult.success(skuVO, "SKU信息修改成功");
    }

    /**
     * 删除单个 SKU
     */
    @Operation(summary = "删除单个 SKU", description = "删除 SPU 矩阵下的单个 SKU 条目，若为该 SPU 下最后一个 SKU 则无法直接删除")
    @DeleteMapping("/{id}")
    @LoginRequired
    public CommonResult<Void> deleteSku(@PathVariable("id") Long id) {
        boolean success = skuService.deleteSku(id);
        if (success) {
            return CommonResult.success(null, "SKU删除成功");
        }
        return CommonResult.failed("SKU删除失败");
    }

    /**
     * 获取单个 SKU 详情
     */
    @Operation(summary = "获取单个 SKU 详情", description = "查询指定 SKU 的详细属性、库存与售价信息")
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<SkuVO> getSkuById(@PathVariable("id") Long id) {
        SkuVO skuVO = skuService.getSkuById(id);
        return CommonResult.success(skuVO);
    }

    /**
     * 查询指定 SPU 下的所有 SKU 列表
     */
    @Operation(summary = "查询指定 SPU 下的所有 SKU 列表", description = "获取某个商品 SPU 关联的全部 SKU 规格矩阵列表")
    @GetMapping("/spu/{spuId}")
    @PassToken
    public CommonResult<List<SkuVO>> listSkuBySpuId(@PathVariable("spuId") Long spuId) {
        List<SkuVO> list = skuService.listSkuBySpuId(spuId);
        return CommonResult.success(list);
    }

    /**
     * 单独调整 SKU 库存 (自动同步 SPU 总库存)
     */
    @Operation(summary = "单独调整 SKU 库存", description = "快捷调整单品库存数，系统自动重新汇总计算 SPU 总库存")
    @PutMapping("/{id}/stock")
    @LoginRequired
    public CommonResult<Void> updateStock(@PathVariable("id") Long id,
                                          @Valid @RequestBody SkuStockUpdateDTO stockDTO) {
        boolean success = skuService.updateStock(id, stockDTO.getStock());
        if (success) {
            return CommonResult.success(null, "SKU库存调整成功");
        }
        return CommonResult.failed("库存调整失败");
    }

    /**
     * 单独调整 SKU 价格 (自动同步 SPU 最低/最高售价)
     */
    @Operation(summary = "单独调整 SKU 价格", description = "快捷微调单品售价、原价与成本价，自动同步 SPU 标示价格区间")
    @PutMapping("/{id}/price")
    @LoginRequired
    public CommonResult<Void> updatePrice(@PathVariable("id") Long id,
                                          @Valid @RequestBody SkuPriceUpdateDTO priceDTO) {
        boolean success = skuService.updatePrice(id, priceDTO.getPrice(), priceDTO.getOriginalPrice(), priceDTO.getCostPrice());
        if (success) {
            return CommonResult.success(null, "SKU价格调整成功");
        }
        return CommonResult.failed("价格调整失败");
    }

    /**
     * 启用/禁用单个 SKU
     */
    @Operation(summary = "启用/禁用单个 SKU", description = "控制单个 SKU 规格在商城的禁售或可售状态")
    @PutMapping("/{id}/status")
    @LoginRequired
    public CommonResult<Void> updateStatus(@PathVariable("id") Long id,
                                           @RequestParam("status") Integer status) {
        boolean success = skuService.updateStatus(id, status);
        if (success) {
            return CommonResult.success(null, "SKU状态更新成功");
        }
        return CommonResult.failed("SKU状态更新失败");
    }
}
