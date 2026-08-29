package com.example.baseboot.module.product.category.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.category.dto.CategoryCreateDTO;
import com.example.baseboot.module.product.category.dto.CategoryUpdateDTO;
import com.example.baseboot.module.product.category.service.CategoryService;
import com.example.baseboot.module.product.category.vo.CategoryTreeVO;
import com.example.baseboot.module.product.category.vo.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 创建商品分类
     */
    @PostMapping
    @LoginRequired
    public CommonResult<CategoryVO> createCategory(@Valid @RequestBody CategoryCreateDTO createDTO) {
        CategoryVO categoryVO = categoryService.createCategory(createDTO);
        return CommonResult.success(categoryVO, "商品分类创建成功");
    }

    /**
     * 修改商品分类
     */
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
    @GetMapping("/{id}")
    @PassToken
    public CommonResult<CategoryVO> getCategoryById(@PathVariable("id") Long id) {
        CategoryVO categoryVO = categoryService.getCategoryById(id);
        return CommonResult.success(categoryVO);
    }

    /**
     * 获取全量分类树状结构
     */
    @GetMapping("/tree")
    @PassToken
    public CommonResult<List<CategoryTreeVO>> listCategoryTree() {
        List<CategoryTreeVO> tree = categoryService.listCategoryTree();
        return CommonResult.success(tree);
    }

    /**
     * 按条件查询分类列表 (平铺列表)
     */
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
