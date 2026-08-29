package com.example.baseboot.module.product.spec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.spec.dto.SpecValueBatchDTO;
import com.example.baseboot.module.product.spec.dto.SpecValueCreateDTO;
import com.example.baseboot.module.product.spec.dto.SpecValueUpdateDTO;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.entity.SpuSpecRelation;
import com.example.baseboot.module.product.spec.mapper.SkuSpecValueMapper;
import com.example.baseboot.module.product.spec.mapper.SpecKeyMapper;
import com.example.baseboot.module.product.spec.mapper.SpecValueMapper;
import com.example.baseboot.module.product.spec.mapper.SpuSpecRelationMapper;
import com.example.baseboot.module.product.spec.service.SpecValueService;
import com.example.baseboot.module.product.spec.vo.SpecValueVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品规格值业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecValueServiceImpl extends ServiceImpl<SpecValueMapper, SpecValue> implements SpecValueService {

    private final SpecKeyMapper specKeyMapper;
    private final SkuSpecValueMapper skuSpecValueMapper;
    private final SpuSpecRelationMapper spuSpecRelationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecValueVO createSpecValue(SpecValueCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格值参数不能为空");
        }

        SpecKey specKey = specKeyMapper.selectById(createDTO.getSpecKeyId());
        if (specKey == null) {
            throw new BusinessException(ResultCode.SPEC_KEY_NOT_EXIST);
        }

        String val = createDTO.getValue().trim();
        long count = this.count(new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecKeyId, createDTO.getSpecKeyId())
                .eq(SpecValue::getValue, val));
        if (count > 0) {
            throw new BusinessException(ResultCode.SPEC_VALUE_EXISTS);
        }

        SpecValue specValue = SpecValue.builder()
                .specKeyId(createDTO.getSpecKeyId())
                .value(val)
                .sort(createDTO.getSort() != null ? createDTO.getSort() : 0)
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(specValue);
        return SpecValueVO.fromEntity(specValue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SpecValueVO> batchCreateSpecValues(SpecValueBatchDTO batchDTO) {
        if (batchDTO == null || CollectionUtils.isEmpty(batchDTO.getValues())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格值列表不能为空");
        }

        SpecKey specKey = specKeyMapper.selectById(batchDTO.getSpecKeyId());
        if (specKey == null) {
            throw new BusinessException(ResultCode.SPEC_KEY_NOT_EXIST);
        }

        List<SpecValueVO> resultList = new ArrayList<>();
        int index = 1;
        for (String val : batchDTO.getValues()) {
            if (!StringUtils.hasText(val)) {
                continue;
            }
            String trimmed = val.trim();
            long count = this.count(new LambdaQueryWrapper<SpecValue>()
                    .eq(SpecValue::getSpecKeyId, batchDTO.getSpecKeyId())
                    .eq(SpecValue::getValue, trimmed));
            if (count == 0) {
                SpecValue specValue = SpecValue.builder()
                        .specKeyId(batchDTO.getSpecKeyId())
                        .value(trimmed)
                        .sort(index++)
                        .status(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                this.save(specValue);
                resultList.add(SpecValueVO.fromEntity(specValue));
            }
        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpecValueVO updateSpecValue(Long id, SpecValueUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格值ID不能为空");
        }
        SpecValue specValue = this.getById(id);
        if (specValue == null) {
            throw new BusinessException(ResultCode.SPEC_VALUE_NOT_EXIST);
        }

        String newVal = updateDTO.getValue().trim();
        if (!newVal.equalsIgnoreCase(specValue.getValue())) {
            long count = this.count(new LambdaQueryWrapper<SpecValue>()
                    .eq(SpecValue::getSpecKeyId, specValue.getSpecKeyId())
                    .eq(SpecValue::getValue, newVal)
                    .ne(SpecValue::getId, id));
            if (count > 0) {
                throw new BusinessException(ResultCode.SPEC_VALUE_EXISTS);
            }
            specValue.setValue(newVal);
        }

        if (updateDTO.getSort() != null) {
            specValue.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            specValue.setStatus(updateDTO.getStatus());
        }
        specValue.setUpdateTime(LocalDateTime.now());

        this.updateById(specValue);
        return SpecValueVO.fromEntity(specValue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpecValue(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格值ID不能为空");
        }
        SpecValue specValue = this.getById(id);
        if (specValue == null) {
            throw new BusinessException(ResultCode.SPEC_VALUE_NOT_EXIST);
        }

        // 检查是否已被商品 SKU 或 SPU 选用
        Long skuUseCount = skuSpecValueMapper.selectCount(new LambdaQueryWrapper<SkuSpecValue>().eq(SkuSpecValue::getSpecValueId, id));
        if (skuUseCount > 0) {
            throw new BusinessException(ResultCode.SPEC_VALUE_IN_USE);
        }

        Long spuUseCount = spuSpecRelationMapper.selectCount(new LambdaQueryWrapper<SpuSpecRelation>().eq(SpuSpecRelation::getSpecValueId, id));
        if (spuUseCount > 0) {
            throw new BusinessException(ResultCode.SPEC_VALUE_IN_USE);
        }

        return this.removeById(id);
    }

    @Override
    public SpecValueVO getSpecValueById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格值ID不能为空");
        }
        SpecValue specValue = this.getById(id);
        if (specValue == null) {
            throw new BusinessException(ResultCode.SPEC_VALUE_NOT_EXIST);
        }
        return SpecValueVO.fromEntity(specValue);
    }

    @Override
    public List<SpecValueVO> listValuesBySpecKeyId(Long specKeyId) {
        if (specKeyId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格项ID不能为空");
        }
        List<SpecValue> list = this.list(new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecKeyId, specKeyId)
                .orderByAsc(SpecValue::getSort)
                .orderByAsc(SpecValue::getId));

        return list.stream().map(SpecValueVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "规格值ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-禁用, 1-启用)");
        }
        SpecValue specValue = this.getById(id);
        if (specValue == null) {
            throw new BusinessException(ResultCode.SPEC_VALUE_NOT_EXIST);
        }
        specValue.setStatus(status);
        specValue.setUpdateTime(LocalDateTime.now());
        return this.updateById(specValue);
    }
}
