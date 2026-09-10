package com.example.baseboot.module.product.category.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.category.dto.CategoryCreateDTO;
import com.example.baseboot.module.product.category.dto.CategoryUpdateDTO;
import com.example.baseboot.module.product.category.service.CategoryService;
import com.example.baseboot.module.product.category.vo.CategoryTreeVO;
import com.example.baseboot.module.product.category.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@Tag(name = "08. 商品类目管理 (CategoryController)", description = "商品多级类目创建、修改、删除、分类树查询与状态启停")
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * 创建商品分类
     */
    @Operation(summary = "创建商品分类", description = "创建新商品分类，支持一级分类与指定 parentId 的多级子类目")
    @PostMapping
    @LoginRequired
    public CommonResult<CategoryVO> createCategory(@Valid @RequestBody CategoryCreateDTO createDTO) {
        CategoryVO categoryVO = categoryService.createCategory(createDTO);
        return CommonResult.success(categoryVO, "商品分类创建成功");
    }

    /**
     * 修改商品分类
     */
    @Operation(summary = "修改商品分类", description = "更新类目名称、父级ID、图标与展示排序")
    @PutMapping("/{id}")
    @LoginRequired
    public CommonResult<CategoryVO> updateCategory(@PathVariable("id") Long id,
                                                   @Valid @RequestBody CategoryUpdateDTO updateDTO) {
        CategoryVO categoryVO = categoryService.updateCategory(id, updateDTO);
        return CommonResult.success(categoryVO, "商品分类修改成功");
    }

    /**
     * 删除商品分类
     */
    @Operation(summary = "删除商品分类", description = "若存在子分类或被 SPU 商品引用则拒绝删除")
    @DeleteMapping("/{id}")
    @LoginRequired
    public CommonResult<Void> deleteCategory(@PathVariable("id") Long id) {
        boolean success = categoryService.deleteCategory(id);
        if (success) {
            return CommonResult.success(null, "商品分类删除成功");
        }
        return CommonResult.failed("商品分类删除失败");
    }

    /**
     * 根据ID获取分类详情
     */
    @Operation(summary = "获取分类详情", description = "根据类目 ID 查询分类详细信息与层级路径")
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<CategoryVO> getCategoryById(@PathVariable("id") Long id) {
        CategoryVO categoryVO = categoryService.getCategoryById(id);
        return CommonResult.success(categoryVO);
    }

    /**
     * 获取全量分类树状结构
     */
    @Operation(summary = "获取全量分类树状结构", description = "返回具有父子嵌套结构的完整三级分类树，用于导航与级联选择")
    @GetMapping("/tree")
    @PassToken
    public CommonResult<List<CategoryTreeVO>> listCategoryTree() {
        List<CategoryTreeVO> tree = categoryService.listCategoryTree();
        return CommonResult.success(tree);
    }

    /**
     * 按条件查询分类列表 (平铺列表)
     */
    @Operation(summary = "平铺条件检索分类列表", description = "支持按 parentId、启用状态及分类名称模糊筛选")
    @GetMapping("/list")
    @PassToken
    public CommonResult<List<CategoryVO>> listCategories(@RequestParam(value = "parentId", required = false) Long parentId,
                                                         @RequestParam(value = "status", required = false) Integer status,
                                                         @RequestParam(value = "name", required = false) String name) {
        List<CategoryVO> list = categoryService.listCategories(parentId, status, name);
        return CommonResult.success(list);
    }

    /**
     * 启用/禁用分类
     */
    @Operation(summary = "启用/禁用分类", description = "切换分类的显示与禁用状态")
    @PutMapping("/{id}/status")
    @LoginRequired
    public CommonResult<Void> updateStatus(@PathVariable("id") Long id,
                                           @RequestParam("status") Integer status) {
        boolean success = categoryService.updateStatus(id, status);
        if (success) {
            return CommonResult.success(null, "分类状态更新成功");
        }
        return CommonResult.failed("分类状态更新失败");
    }
}
