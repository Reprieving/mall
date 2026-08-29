package com.example.baseboot.module.product.spec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.module.product.spec.dto.SkuSpecValueItemDTO;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.mapper.SkuSpecValueMapper;
import com.example.baseboot.module.product.spec.mapper.SpecKeyMapper;
import com.example.baseboot.module.product.spec.mapper.SpecValueMapper;
import com.example.baseboot.module.product.spec.service.SkuSpecValueService;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;
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
 * SKU 规格绑定关系业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkuSpecValueServiceImpl extends ServiceImpl<SkuSpecValueMapper, SkuSpecValue> implements SkuSpecValueService {

    private final SpecKeyMapper specKeyMapper;
    private final SpecValueMapper specValueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuSpecValues(Long spuId, Long skuId, List<SkuSpecValueItemDTO> specValueItems) {
        if (skuId == null) {
            return;
        }

        // 先清理该 SKU 已有的绑定关系
        this.remove(new LambdaQueryWrapper<SkuSpecValue>().eq(SkuSpecValue::getSkuId, skuId));

        if (CollectionUtils.isEmpty(specValueItems)) {
            return;
        }

        for (SkuSpecValueItemDTO item : specValueItems) {
            if (item.getSpecKeyId() == null || item.getSpecValueId() == null) {
                continue;
            }

            String keyName = item.getSpecKeyName();
            if (!StringUtils.hasText(keyName)) {
                SpecKey key = specKeyMapper.selectById(item.getSpecKeyId());
                keyName = key != null ? key.getName() : "";
            }

            String val = item.getSpecValue();
            if (!StringUtils.hasText(val)) {
                SpecValue valueEntity = specValueMapper.selectById(item.getSpecValueId());
                val = valueEntity != null ? valueEntity.getValue() : "";
            }

            SkuSpecValue record = SkuSpecValue.builder()
                    .skuId(skuId)
                    .spuId(spuId)
                    .specKeyId(item.getSpecKeyId())
                    .specKeyName(keyName)
                    .specValueId(item.getSpecValueId())
                    .specValue(val)
                    .createTime(LocalDateTime.now())
                    .build();

            this.save(record);
        }
    }

    @Override
    public List<SkuSpecValueVO> listBySkuId(Long skuId) {
        if (skuId == null) {
            return Collections.emptyList();
        }
        List<SkuSpecValue> list = this.list(new LambdaQueryWrapper<SkuSpecValue>()
                .eq(SkuSpecValue::getSkuId, skuId)
                .orderByAsc(SkuSpecValue::getId));

        return list.stream().map(SkuSpecValueVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<SkuSpecValueVO>> mapBySkuIds(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        List<SkuSpecValue> list = this.list(new LambdaQueryWrapper<SkuSpecValue>()
                .in(SkuSpecValue::getSkuId, skuIds)
                .orderByAsc(SkuSpecValue::getId));

        return list.stream()
                .map(SkuSpecValueVO::fromEntity)
                .collect(Collectors.groupingBy(SkuSpecValueVO::getSpecKeyId,
                        Collectors.mapping(v -> v, Collectors.toList())))
                .entrySet().stream()
                .flatMap(e -> list.stream())
                .collect(Collectors.groupingBy(SkuSpecValue::getSkuId,
                        Collectors.mapping(SkuSpecValueVO::fromEntity, Collectors.toList())));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySpuId(Long spuId) {
        if (spuId != null) {
            this.remove(new LambdaQueryWrapper<SkuSpecValue>().eq(SkuSpecValue::getSpuId, spuId));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBySkuId(Long skuId) {
        if (skuId != null) {
            this.remove(new LambdaQueryWrapper<SkuSpecValue>().eq(SkuSpecValue::getSkuId, skuId));
        }
    }
}
