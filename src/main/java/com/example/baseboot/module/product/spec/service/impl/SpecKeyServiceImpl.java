package com.example.baseboot.module.product.spec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.mapper.CategoryMapper;
import com.example.baseboot.module.product.spec.dto.SpecKeyCreateDTO;
import com.example.baseboot.module.product.spec.dto.SpecKeyQueryDTO;
import com.example.baseboot.module.product.spec.dto.SpecKeyUpdateDTO;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.entity.SpuSpecRelation;
import com.example.baseboot.module.product.spec.mapper.SkuSpecValueMapper;
import com.example.baseboot.module.product.spec.mapper.SpecKeyMapper;
import com.example.baseboot.module.product.spec.mapper.SpecValueMapper;
import com.example.baseboot.module.product.spec.mapper.SpuSpecRelationMapper;
import com.example.baseboot.module.product.spec.service.SpecKeyService;
import com.example.baseboot.module.product.spec.vo.SpecKeyVO;
import com.example.baseboot.module.product.spec.vo.SpecValueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品规格项业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecKeyServiceImpl extends ServiceImpl<SpecKeyMapper, SpecKey> implements SpecKeyService {

    private final SpecValueMapper specValueMapper;
    private final SkuSpecValueMapper skuSpecValueMapper;
    private final SpuSpecRelationMapper spuSpecRelationMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecKeyVO createSpecKey(SpecKeyCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格项参数不能为空");
        }

        Long categoryId = createDTO.getCategoryId() != null ? createDTO.getCategoryId() : 0L;
        String name = createDTO.getName().trim();

        // 校验同分类下规格项重名
        long count = this.count(new LambdaQueryWrapper<SpecKey>()
                .eq(SpecKey::getCategoryId, categoryId)
                .eq(SpecKey::getName, name));
        if (count > 0) {
            throw new BusinessException(ResultCode.SPEC_KEY_NAME_EXISTS);
        }

        SpecKey specKey = SpecKey.builder()
                .categoryId(categoryId)
                .name(name)
                .sort(createDTO.getSort() != null ? createDTO.getSort() : 0)
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(specKey);

        // 如果传入了初始规格值列表，一并保存
        List<SpecValueVO> valueVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(createDTO.getInitialValues())) {
            int sortIndex = 1;
            for (String val : createDTO.getInitialValues()) {
                if (StringUtils.hasText(val)) {
                    SpecValue specValue = SpecValue.builder()
                            .specKeyId(specKey.getId())
                            .value(val.trim())
                            .sort(sortIndex++)
                            .status(1)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    specValueMapper.insert(specValue);
                    valueVOList.add(SpecValueVO.fromEntity(specValue));
                }
            }
        }

        SpecKeyVO vo = SpecKeyVO.fromEntity(specKey);
        vo.setValues(valueVOList);
        if (categoryId > 0) {
            Category cat = categoryMapper.selectById(categoryId);
            if (cat != null) {
                vo.setCategoryName(cat.getName());
            }
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecKeyVO updateSpecKey(Long id, SpecKeyUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格项ID不能为空");
        }
        SpecKey specKey = this.getById(id);
        if (specKey == null) {
            throw new BusinessException(ResultCode.SPEC_KEY_NOT_EXIST);
        }

        Long categoryId = updateDTO.getCategoryId() != null ? updateDTO.getCategoryId() : specKey.getCategoryId();
        String newName = updateDTO.getName().trim();

        if (!newName.equalsIgnoreCase(specKey.getName()) || !categoryId.equals(specKey.getCategoryId())) {
            long count = this.count(new LambdaQueryWrapper<SpecKey>()
                    .eq(SpecKey::getCategoryId, categoryId)
                    .eq(SpecKey::getName, newName)
                    .ne(SpecKey::getId, id));
            if (count > 0) {
                throw new BusinessException(ResultCode.SPEC_KEY_NAME_EXISTS);
            }
        }

        specKey.setCategoryId(categoryId);
        specKey.setName(newName);
        if (updateDTO.getSort() != null) {
            specKey.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            specKey.setStatus(updateDTO.getStatus());
        }
        specKey.setUpdateTime(LocalDateTime.now());

        this.updateById(specKey);
        return getSpecKeyById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpecKey(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格项ID不能为空");
        }
        SpecKey specKey = this.getById(id);
        if (specKey == null) {
            throw new BusinessException(ResultCode.SPEC_KEY_NOT_EXIST);
        }

        // 检查是否已被商品 SPU 或 SKU 使用
        Long skuUseCount = skuSpecValueMapper.selectCount(new LambdaQueryWrapper<SkuSpecValue>().eq(SkuSpecValue::getSpecKeyId, id));
        if (skuUseCount > 0) {
            throw new BusinessException(ResultCode.SPEC_KEY_IN_USE);
        }

        Long spuUseCount = spuSpecRelationMapper.selectCount(new LambdaQueryWrapper<SpuSpecRelation>().eq(SpuSpecRelation::getSpecKeyId, id));
        if (spuUseCount > 0) {
            throw new BusinessException(ResultCode.SPEC_KEY_IN_USE);
        }

        // 清理旗下的规格值
        specValueMapper.delete(new LambdaQueryWrapper<SpecValue>().eq(SpecValue::getSpecKeyId, id));

        return this.removeById(id);
    }

    @Override
    public SpecKeyVO getSpecKeyById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格项ID不能为空");
        }
        SpecKey specKey = this.getById(id);
        if (specKey == null) {
            throw new BusinessException(ResultCode.SPEC_KEY_NOT_EXIST);
        }

        List<SpecValue> values = specValueMapper.selectList(new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecKeyId, id)
                .orderByAsc(SpecValue::getSort)
                .orderByAsc(SpecValue::getId));

        SpecKeyVO vo = SpecKeyVO.fromEntity(specKey);
        vo.setValues(values.stream().map(SpecValueVO::fromEntity).collect(Collectors.toList()));
        if (specKey.getCategoryId() != null && specKey.getCategoryId() > 0) {
            Category category = categoryMapper.selectById(specKey.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }

    @Override
    public CommonPage<SpecKeyVO> pageSpecKeys(SpecKeyQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new SpecKeyQueryDTO();
        }
        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<SpecKey> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SpecKey> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(SpecKey::getCategoryId, queryDTO.getCategoryId());
        }
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(SpecKey::getName, queryDTO.getName().trim());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SpecKey::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByAsc(SpecKey::getSort).orderByDesc(SpecKey::getId);

        Page<SpecKey> resultPage = this.page(page, wrapper);
        if (CollectionUtils.isEmpty(resultPage.getRecords())) {
            return CommonPage.restPage(resultPage, Collections.emptyList());
        }

        List<Long> keyIds = resultPage.getRecords().stream().map(SpecKey::getId).collect(Collectors.toList());
        List<SpecValue> allValues = specValueMapper.selectList(new LambdaQueryWrapper<SpecValue>()
                .in(SpecValue::getSpecKeyId, keyIds)
                .orderByAsc(SpecValue::getSort));

        Map<Long, List<SpecValueVO>> valueMap = allValues.stream()
                .map(SpecValueVO::fromEntity)
                .collect(Collectors.groupingBy(SpecValueVO::getSpecKeyId));

        Set<Long> categoryIds = resultPage.getRecords().stream()
                .map(SpecKey::getCategoryId)
                .filter(c -> c != null && c > 0)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            if (categories != null) {
                categoryNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
            }
        }

        Map<Long, String> finalCatMap = categoryNameMap;
        List<SpecKeyVO> voList = resultPage.getRecords().stream().map(k -> {
            SpecKeyVO vo = SpecKeyVO.fromEntity(k);
            vo.setValues(valueMap.getOrDefault(k.getId(), Collections.emptyList()));
            vo.setCategoryName(finalCatMap.get(k.getCategoryId()));
            return vo;
        }).collect(Collectors.toList());

        return CommonPage.restPage(resultPage, voList);
    }

    @Override
    public List<SpecKeyVO> listSpecsByCategory(Long categoryId) {
        LambdaQueryWrapper<SpecKey> wrapper = new LambdaQueryWrapper<SpecKey>()
                .eq(SpecKey::getStatus, 1)
                .orderByAsc(SpecKey::getSort)
                .orderByAsc(SpecKey::getId);

        if (categoryId != null && categoryId > 0) {
            wrapper.and(w -> w.eq(SpecKey::getCategoryId, categoryId).or().eq(SpecKey::getCategoryId, 0));
        } else {
            wrapper.eq(SpecKey::getCategoryId, 0);
        }

        List<SpecKey> keys = this.list(wrapper);
        if (CollectionUtils.isEmpty(keys)) {
            return Collections.emptyList();
        }

        List<Long> keyIds = keys.stream().map(SpecKey::getId).collect(Collectors.toList());
        List<SpecValue> values = specValueMapper.selectList(new LambdaQueryWrapper<SpecValue>()
                .in(SpecValue::getSpecKeyId, keyIds)
                .eq(SpecValue::getStatus, 1)
                .orderByAsc(SpecValue::getSort)
                .orderByAsc(SpecValue::getId));

        Map<Long, List<SpecValueVO>> valueMap = values.stream()
                .map(SpecValueVO::fromEntity)
                .collect(Collectors.groupingBy(SpecValueVO::getSpecKeyId));

        return keys.stream().map(k -> {
            SpecKeyVO vo = SpecKeyVO.fromEntity(k);
            vo.setValues(valueMap.getOrDefault(k.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格项ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-禁用, 1-启用)");
        }
        SpecKey specKey = this.getById(id);
        if (specKey == null) {
            throw new BusinessException(ResultCode.SPEC_KEY_NOT_EXIST);
        }
        specKey.setStatus(status);
        specKey.setUpdateTime(LocalDateTime.now());
        return this.updateById(specKey);
    }
}
