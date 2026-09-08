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

        Set<Long> processedKeyIds = new HashSet<>();
        List<SkuSpecValue> toInsert = new ArrayList<>();

        for (SkuSpecValueItemDTO item : specValueItems) {
            if (item == null) {
                continue;
            }

            Long keyId = item.getSpecKeyId();
            String keyName = item.getSpecKeyName();
            Long valueId = item.getSpecValueId();
            String val = item.getSpecValue();

            // 1. 若未传 specKeyId 但传了名称，自动按名称检索或新建 SpecKey
            if (keyId == null && StringUtils.hasText(keyName)) {
                SpecKey existKey = specKeyMapper.selectOne(new LambdaQueryWrapper<SpecKey>()
                        .eq(SpecKey::getName, keyName.trim())
                        .last("LIMIT 1"));
                if (existKey == null) {
                    existKey = SpecKey.builder()
                            .categoryId(0L)
                            .name(keyName.trim())
                            .sort(0)
                            .status(1)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    specKeyMapper.insert(existKey);
                }
                keyId = existKey.getId();
                keyName = existKey.getName();
            } else if (keyId != null && !StringUtils.hasText(keyName)) {
                SpecKey key = specKeyMapper.selectById(keyId);
                keyName = key != null ? key.getName() : "";
            }

            if (keyId == null) {
                continue;
            }

            // 2. 若未传 specValueId 但传了文本，自动按值检索或新建 SpecValue
            if (valueId == null && StringUtils.hasText(val)) {
                SpecValue existVal = specValueMapper.selectOne(new LambdaQueryWrapper<SpecValue>()
                        .eq(SpecValue::getSpecKeyId, keyId)
                        .eq(SpecValue::getValue, val.trim())
                        .last("LIMIT 1"));
                if (existVal == null) {
                    existVal = SpecValue.builder()
                            .specKeyId(keyId)
                            .value(val.trim())
                            .sort(0)
                            .status(1)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    specValueMapper.insert(existVal);
                }
                valueId = existVal.getId();
                val = existVal.getValue();
            } else if (valueId != null && !StringUtils.hasText(val)) {
                SpecValue valueEntity = specValueMapper.selectById(valueId);
                val = valueEntity != null ? valueEntity.getValue() : "";
            }

            if (valueId == null) {
                continue;
            }

            // 防止同一个 SKU 针对同一规格维度重复绑定
            if (!processedKeyIds.add(keyId)) {
                continue;
            }

            toInsert.add(SkuSpecValue.builder()
                    .skuId(skuId)
                    .spuId(spuId)
                    .specKeyId(keyId)
                    .specKeyName(keyName)
                    .specValueId(valueId)
                    .specValue(val)
                    .createTime(LocalDateTime.now())
                    .build());
        }

        if (!toInsert.isEmpty()) {
            this.saveBatch(toInsert);
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

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

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

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }

        return list.stream().collect(Collectors.groupingBy(
                SkuSpecValue::getSkuId,
                Collectors.mapping(SkuSpecValueVO::fromEntity, Collectors.toList())
        ));
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
