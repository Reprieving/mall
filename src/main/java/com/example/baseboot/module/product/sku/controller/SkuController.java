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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 SKU 控制器
 */
@RestController
@RequestMapping("/api/sku")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    /**
     * 为指定 SPU 新增单个 SKU (自动联动汇总 SPU 价格与总库存)
     */
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
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<SkuVO> getSkuById(@PathVariable("id") Long id) {
        SkuVO skuVO = skuService.getSkuById(id);
        return CommonResult.success(skuVO);
    }

    /**
     * 查询指定 SPU 下的所有 SKU 列表
     */
    @GetMapping("/spu/{spuId}")
    @PassToken
    public CommonResult<List<SkuVO>> listSkuBySpuId(@PathVariable("spuId") Long spuId) {
        List<SkuVO> list = skuService.listSkuBySpuId(spuId);
        return CommonResult.success(list);
    }

    /**
     * 单独调整 SKU 库存 (自动同步 SPU 总库存)
     */
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
