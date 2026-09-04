package com.example.baseboot.module.product.spec.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.spec.dto.*;
import com.example.baseboot.module.product.spec.service.SpecKeyService;
import com.example.baseboot.module.product.spec.service.SpecValueService;
import com.example.baseboot.module.product.spec.vo.SpecKeyVO;
import com.example.baseboot.module.product.spec.vo.SpecValueVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品规格项与规格值管理控制器
 */
@Tag(name = "10. 商品规格属性管理 (SpecController)", description = "规格项定义、分类绑定模板、规格值维护与批量录入")
@RestController
@RequestMapping("/api/spec")
@RequiredArgsConstructor
public class SpecController {

    private final SpecKeyService specKeyService;
    private final SpecValueService specValueService;

    // ==========================================
    // 规格项 (Spec Key) 接口
    // ==========================================

    /**
     * 创建规格项 (支持附带初始规格值列表)
     */
    @Operation(summary = "创建规格项", description = "新建规格属性项（如：颜色、尺码、内存），支持同时附带初始规格值集合")
    @PostMapping("/key")
    @LoginRequired
    public CommonResult<SpecKeyVO> createSpecKey(@Valid @RequestBody SpecKeyCreateDTO createDTO) {
        SpecKeyVO vo = specKeyService.createSpecKey(createDTO);
        return CommonResult.success(vo, "规格项创建成功");
    }

    /**
     * 修改规格项
     */
    @Operation(summary = "修改规格项", description = "修改规格项名称、所属分类与排序")
    @PutMapping("/key/{id}")
    @LoginRequired
    public CommonResult<SpecKeyVO> updateSpecKey(@PathVariable("id") Long id,
                                                 @Valid @RequestBody SpecKeyUpdateDTO updateDTO) {
        SpecKeyVO vo = specKeyService.updateSpecKey(id, updateDTO);
        return CommonResult.success(vo, "规格项修改成功");
    }

    /**
     * 删除规格项 (引用校验)
     */
    @Operation(summary = "删除规格项", description = "删除规格项定义，若被已有商品规格引用则不可删除")
    @DeleteMapping("/key/{id}")
    @LoginRequired
    public CommonResult<Void> deleteSpecKey(@PathVariable("id") Long id) {
        boolean success = specKeyService.deleteSpecKey(id);
        if (success) {
            return CommonResult.success(null, "规格项删除成功");
        }
        return CommonResult.failed("规格项删除失败");
    }

    /**
     * 获取规格项详情 (含规格值列表)
     */
    @Operation(summary = "获取规格项详情", description = "按 ID 查询规格项并级联返回其下挂载的所有可用规格值列表")
    @GetMapping("/key/{id}")
    @PassToken
    public CommonResult<SpecKeyVO> getSpecKeyById(@PathVariable("id") Long id) {
        SpecKeyVO vo = specKeyService.getSpecKeyById(id);
        return CommonResult.success(vo);
    }

    /**
     * 分页查询规格项列表
     */
    @Operation(summary = "分页查询规格项列表", description = "支持按分类 ID、关键字及状态筛选规格项")
    @GetMapping("/key/page")
    @PassToken
    public CommonResult<CommonPage<SpecKeyVO>> pageSpecKeys(SpecKeyQueryDTO queryDTO) {
        CommonPage<SpecKeyVO> page = specKeyService.pageSpecKeys(queryDTO);
        return CommonResult.success(page);
    }

    /**
     * 根据分类 ID 查询该分类下所有可用的规格模板及可选值
     */
    @Operation(summary = "按分类查询规格模板", description = "在商品发布或编辑页面，根据类目获取所推荐使用的标准规格项与可选规格值")
    @GetMapping("/category/{categoryId}")
    @PassToken
    public CommonResult<List<SpecKeyVO>> listSpecsByCategory(@PathVariable("categoryId") Long categoryId) {
        List<SpecKeyVO> list = specKeyService.listSpecsByCategory(categoryId);
        return CommonResult.success(list);
    }

    /**
     * 启用/禁用规格项
     */
    @Operation(summary = "启用/禁用规格项", description = "切换规格项的可用状态")
    @PutMapping("/key/{id}/status")
    @LoginRequired
    public CommonResult<Void> updateKeyStatus(@PathVariable("id") Long id,
                                              @RequestParam("status") Integer status) {
        boolean success = specKeyService.updateStatus(id, status);
        if (success) {
            return CommonResult.success(null, "规格项状态更新成功");
        }
        return CommonResult.failed("规格项状态更新失败");
    }

    // ==========================================
    // 规格值 (Spec Value) 接口
    // ==========================================

    /**
     * 新增单个规格值
     */
    @Operation(summary = "新增单个规格值", description = "向指定规格项下追加单个可选规格值（如：黑色、XL）")
    @PostMapping("/value")
    @LoginRequired
    public CommonResult<SpecValueVO> createSpecValue(@Valid @RequestBody SpecValueCreateDTO createDTO) {
        SpecValueVO vo = specValueService.createSpecValue(createDTO);
        return CommonResult.success(vo, "规格值创建成功");
    }

    /**
     * 批量新增规格值
     */
    @Operation(summary = "批量新增规格值", description = "一次性向指定规格项批量录入多个规格取值")
    @PostMapping("/value/batch")
    @LoginRequired
    public CommonResult<List<SpecValueVO>> batchCreateSpecValues(@Valid @RequestBody SpecValueBatchDTO batchDTO) {
        List<SpecValueVO> list = specValueService.batchCreateSpecValues(batchDTO);
        return CommonResult.success(list, "批量创建规格值成功");
    }

    /**
     * 修改规格值
     */
    @Operation(summary = "修改规格值", description = "修改规格值的文字描述或展示顺序")
    @PutMapping("/value/{id}")
    @LoginRequired
    public CommonResult<SpecValueVO> updateSpecValue(@PathVariable("id") Long id,
                                                     @Valid @RequestBody SpecValueUpdateDTO updateDTO) {
        SpecValueVO vo = specValueService.updateSpecValue(id, updateDTO);
        return CommonResult.success(vo, "规格值修改成功");
    }

    /**
     * 删除规格值 (引用校验)
     */
    @Operation(summary = "删除规格值", description = "若规格值已被 SKU 引用则不可删除")
    @DeleteMapping("/value/{id}")
    @LoginRequired
    public CommonResult<Void> deleteSpecValue(@PathVariable("id") Long id) {
        boolean success = specValueService.deleteSpecValue(id);
        if (success) {
            return CommonResult.success(null, "规格值删除成功");
        }
        return CommonResult.failed("规格值删除失败");
    }

    /**
     * 获取单个规格值详情
     */
    @Operation(summary = "获取单个规格值详情", description = "查询规格值详情及其所属规格项 ID")
    @GetMapping("/value/{id}")
    @PassToken
    public CommonResult<SpecValueVO> getSpecValueById(@PathVariable("id") Long id) {
        SpecValueVO vo = specValueService.getSpecValueById(id);
        return CommonResult.success(vo);
    }

    /**
     * 查询指定规格项下的所有规格值列表
     */
    @Operation(summary = "查询规格项下的全部规格值", description = "按规格项 keyId 获取该项下的所有候选属性值清单")
    @GetMapping("/key/{keyId}/values")
    @PassToken
    public CommonResult<List<SpecValueVO>> listValuesBySpecKeyId(@PathVariable("keyId") Long keyId) {
        List<SpecValueVO> list = specValueService.listValuesBySpecKeyId(keyId);
        return CommonResult.success(list);
    }

    /**
     * 启用/禁用规格值
     */
    @Operation(summary = "启用/禁用规格值", description = "切换规格值的可用性")
    @PutMapping("/value/{id}/status")
    @LoginRequired
    public CommonResult<Void> updateValueStatus(@PathVariable("id") Long id,
                                                @RequestParam("status") Integer status) {
        boolean success = specValueService.updateStatus(id, status);
        if (success) {
            return CommonResult.success(null, "规格值状态更新成功");
        }
        return CommonResult.failed("规格值状态更新失败");
    }
}
