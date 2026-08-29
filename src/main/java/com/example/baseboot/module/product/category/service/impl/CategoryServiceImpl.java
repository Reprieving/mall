package com.example.baseboot.module.product.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.category.dto.CategoryCreateDTO;
import com.example.baseboot.module.product.category.dto.CategoryUpdateDTO;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.mapper.CategoryMapper;
import com.example.baseboot.module.product.category.service.CategoryService;
import com.example.baseboot.module.product.category.vo.CategoryTreeVO;
import com.example.baseboot.module.product.category.vo.CategoryVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品分类业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Lazy
    private final SpuMapper spuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO createCategory(CategoryCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类参数不能为空");
        }

        int level = 1;
        if (createDTO.getParentId() != null && createDTO.getParentId() > 0) {
            Category parent = this.getById(createDTO.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST, "父级分类不存在");
            }
            level = parent.getLevel() != null ? parent.getLevel() + 1 : 2;
        }

        Category category = Category.builder()
                .parentId(createDTO.getParentId() != null ? createDTO.getParentId() : 0L)
                .name(createDTO.getName().trim())
                .level(createDTO.getLevel() != null ? createDTO.getLevel() : level)
                .icon(createDTO.getIcon())
                .sort(createDTO.getSort() != null ? createDTO.getSort() : 0)
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(category);
        return CategoryVO.fromEntity(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CategoryVO updateCategory(Long id, CategoryUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类ID不能为空");
        }
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        if (updateDTO.getParentId() != null && !updateDTO.getParentId().equals(category.getParentId())) {
            if (updateDTO.getParentId().equals(id)) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "父级分类不能是当前分类本身");
            }
            if (updateDTO.getParentId() > 0) {
                Category parent = this.getById(updateDTO.getParentId());
                if (parent == null) {
                    throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST, "父级分类不存在");
                }
                category.setLevel(parent.getLevel() != null ? parent.getLevel() + 1 : 2);
            } else {
                category.setLevel(1);
            }
            category.setParentId(updateDTO.getParentId());
        }

        if (updateDTO.getLevel() != null) {
            category.setLevel(updateDTO.getLevel());
        }
        if (StringUtils.hasText(updateDTO.getName())) {
            category.setName(updateDTO.getName().trim());
        }
        if (updateDTO.getIcon() != null) {
            category.setIcon(updateDTO.getIcon());
        }
        if (updateDTO.getSort() != null) {
            category.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            category.setStatus(updateDTO.getStatus());
        }
        category.setUpdateTime(LocalDateTime.now());

        this.updateById(category);
        return CategoryVO.fromEntity(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类ID不能为空");
        }
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        // 检查是否有子分类
        long childCount = this.count(new LambdaQueryWrapper<Category>().eq(Category::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.CATEGORY_HAS_CHILDREN);
        }

        // 检查是否有挂载的 SPU
        if (spuMapper != null) {
            long spuCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getCategoryId, id));
            if (spuCount > 0) {
                throw new BusinessException(ResultCode.CATEGORY_HAS_PRODUCTS);
            }
        }

        return this.removeById(id);
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类ID不能为空");
        }
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
        return CategoryVO.fromEntity(category);
    }

    @Override
    public List<CategoryTreeVO> listCategoryTree() {
        List<Category> allCategories = this.list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));

        if (allCategories == null || allCategories.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, CategoryTreeVO> nodeMap = new HashMap<>(allCategories.size());
        List<CategoryTreeVO> rootNodes = new ArrayList<>();

        // 初始化节点字典
        for (Category cat : allCategories) {
            nodeMap.put(cat.getId(), CategoryTreeVO.fromEntity(cat));
        }

        // 组装树形层级
        for (Category cat : allCategories) {
            CategoryTreeVO currentNode = nodeMap.get(cat.getId());
            Long parentId = cat.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                rootNodes.add(currentNode);
            } else {
                CategoryTreeVO parentNode = nodeMap.get(parentId);
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList<>());
                }
                parentNode.getChildren().add(currentNode);
            }
        }

        return rootNodes;
    }

    @Override
    public List<CategoryVO> listCategories(Long parentId, Integer status, String name) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            wrapper.eq(Category::getParentId, parentId);
        }
        if (status != null) {
            wrapper.eq(Category::getStatus, status);
        }
        if (StringUtils.hasText(name)) {
            wrapper.like(Category::getName, name.trim());
        }
        wrapper.orderByAsc(Category::getSort).orderByAsc(Category::getId);

        List<Category> list = this.list(wrapper);
        return list.stream().map(CategoryVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-禁用, 1-启用)");
        }
        Category category = this.getById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        return this.updateById(category);
    }
}
