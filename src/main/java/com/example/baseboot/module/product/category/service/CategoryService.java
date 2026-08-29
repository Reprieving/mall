package com.example.baseboot.module.product.category.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.module.product.category.dto.CategoryCreateDTO;
import com.example.baseboot.module.product.category.dto.CategoryUpdateDTO;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.vo.CategoryTreeVO;
import com.example.baseboot.module.product.category.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类业务接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 创建商品分类
     */
    CategoryVO createCategory(CategoryCreateDTO createDTO);

    /**
     * 修改商品分类
     */
    CategoryVO updateCategory(Long id, CategoryUpdateDTO updateDTO);

    /**
     * 删除商品分类
     */
    boolean deleteCategory(Long id);

    /**
     * 根据ID获取分类详情
     */
    CategoryVO getCategoryById(Long id);

    /**
     * 获取全量分类树状结构
     */
    List<CategoryTreeVO> listCategoryTree();

    /**
     * 按条件查询分类列表 (平铺)
     */
    List<CategoryVO> listCategories(Long parentId, Integer status, String name);

    /**
     * 修改分类启用状态
     */
    boolean updateStatus(Long id, Integer status);
}
