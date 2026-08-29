package com.example.baseboot.module.product.sku.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
import com.example.baseboot.module.product.sku.dto.SkuUpdateDTO;
import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.sku.mapper.SkuMapper;
import com.example.baseboot.module.product.sku.service.SkuService;
import com.example.baseboot.module.product.sku.vo.SkuVO;
import com.example.baseboot.module.product.spec.service.SkuSpecValueService;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.product.spu.service.SpuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品 SKU 业务实现类 (基于方案 B 全关系型绑定)
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Lazy
    private final SpuService spuService;
    private final SpuMapper spuMapper;
    private final SkuSpecValueService skuSpecValueService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkuVO createSku(Long spuId, SkuItemDTO skuDTO) {
        if (spuId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU ID不能为空");
        }
        if (skuDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU参数不能为空");
        }

        Spu spu = spuMapper.selectById(spuId);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }

        String skuCode = skuDTO.getSkuCode().trim();
        long count = this.count(new LambdaQueryWrapper<Sku>().eq(Sku::getSkuCode, skuCode));
        if (count > 0) {
            throw new BusinessException(ResultCode.SKU_CODE_EXISTS);
        }

        String specData = skuDTO.getSpecData();
        if (!StringUtils.hasText(specData) && !CollectionUtils.isEmpty(skuDTO.getSpecValues())) {
            try {
                specData = OBJECT_MAPPER.writeValueAsString(skuDTO.getSpecValues());
            } catch (Exception ignored) {
            }
        }

        Sku sku = Sku.builder()
                .spuId(spuId)
                .skuCode(skuCode)
                .name(skuDTO.getName().trim())
                .pic(skuDTO.getPic())
                .price(skuDTO.getPrice())
                .originalPrice(skuDTO.getOriginalPrice())
                .costPrice(skuDTO.getCostPrice())
                .stock(skuDTO.getStock() != null ? skuDTO.getStock() : 0)
                .lockStock(0)
                .weight(skuDTO.getWeight() != null ? skuDTO.getWeight() : BigDecimal.ZERO)
                .volume(skuDTO.getVolume() != null ? skuDTO.getVolume() : BigDecimal.ZERO)
                .specData(specData)
                .status(skuDTO.getStatus() != null ? skuDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(sku);

        // 强关系绑定保存到 pms_sku_spec_value
        skuSpecValueService.saveSkuSpecValues(spuId, sku.getId(), skuDTO.getSpecValues());

        // 同步重新计算 SPU 的价格区间与总库存
        spuService.recalculateSpuStockAndPrice(spuId);

        SkuVO skuVO = SkuVO.fromEntity(sku);
        skuVO.setSpecValues(skuSpecValueService.listBySkuId(sku.getId()));
        return skuVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkuVO updateSku(Long id, SkuUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU ID不能为空");
        }
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }

        if (StringUtils.hasText(updateDTO.getSkuCode())) {
            String newCode = updateDTO.getSkuCode().trim();
            if (!newCode.equalsIgnoreCase(sku.getSkuCode())) {
                long count = this.count(new LambdaQueryWrapper<Sku>()
                        .eq(Sku::getSkuCode, newCode)
                        .ne(Sku::getId, id));
                if (count > 0) {
                    throw new BusinessException(ResultCode.SKU_CODE_EXISTS);
                }
                sku.setSkuCode(newCode);
            }
        }

        String specData = updateDTO.getSpecData();
        if (!StringUtils.hasText(specData) && !CollectionUtils.isEmpty(updateDTO.getSpecValues())) {
            try {
                specData = OBJECT_MAPPER.writeValueAsString(updateDTO.getSpecValues());
            } catch (Exception ignored) {
            }
        }

        sku.setName(updateDTO.getName().trim());
        sku.setPic(updateDTO.getPic());
        sku.setPrice(updateDTO.getPrice());
        sku.setOriginalPrice(updateDTO.getOriginalPrice());
        sku.setCostPrice(updateDTO.getCostPrice());
        sku.setStock(updateDTO.getStock());
        if (updateDTO.getWeight() != null) {
            sku.setWeight(updateDTO.getWeight());
        }
        if (updateDTO.getVolume() != null) {
            sku.setVolume(updateDTO.getVolume());
        }
        if (specData != null) {
            sku.setSpecData(specData);
        }
        if (updateDTO.getStatus() != null) {
            sku.setStatus(updateDTO.getStatus());
        }
        sku.setUpdateTime(LocalDateTime.now());

        this.updateById(sku);

        // 如果传入了规格键值绑定信息，同步维护 pms_sku_spec_value
        if (updateDTO.getSpecValues() != null) {
            skuSpecValueService.saveSkuSpecValues(sku.getSpuId(), sku.getId(), updateDTO.getSpecValues());
        }

        // 同步更新 SPU 价格与库存汇总
        spuService.recalculateSpuStockAndPrice(sku.getSpuId());

        SkuVO skuVO = SkuVO.fromEntity(sku);
        skuVO.setSpecValues(skuSpecValueService.listBySkuId(sku.getId()));
        return skuVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSku(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU ID不能为空");
        }
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }

        Long spuId = sku.getSpuId();
        skuSpecValueService.deleteBySkuId(id);
        boolean removed = this.removeById(id);
        if (removed) {
            spuService.recalculateSpuStockAndPrice(spuId);
        }
        return removed;
    }

    @Override
    public SkuVO getSkuById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU ID不能为空");
        }
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
        SkuVO vo = SkuVO.fromEntity(sku);
        vo.setSpecValues(skuSpecValueService.listBySkuId(id));
        return vo;
    }

    @Override
    public List<SkuVO> listSkuBySpuId(Long spuId) {
        if (spuId == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU ID不能为空");
        }
        List<Sku> list = this.list(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .orderByAsc(Sku::getId));

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<Long> skuIds = list.stream().map(Sku::getId).collect(Collectors.toList());
        Map<Long, List<SkuSpecValueVO>> specMap = skuSpecValueService.mapBySkuIds(skuIds);

        return list.stream().map(s -> {
            SkuVO vo = SkuVO.fromEntity(s);
            vo.setSpecValues(specMap.getOrDefault(s.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(Long id, Integer stock) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU ID不能为空");
        }
        if (stock == null || stock < 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "库存数量不能为负数");
        }
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
        sku.setStock(stock);
        sku.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(sku);
        if (updated) {
            spuService.recalculateSpuStockAndPrice(sku.getSpuId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePrice(Long id, BigDecimal price, BigDecimal originalPrice, BigDecimal costPrice) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU ID不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "销售价格不能小于0");
        }
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
        sku.setPrice(price);
        if (originalPrice != null) {
            sku.setOriginalPrice(originalPrice);
        }
        if (costPrice != null) {
            sku.setCostPrice(costPrice);
        }
        sku.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(sku);
        if (updated) {
            spuService.recalculateSpuStockAndPrice(sku.getSpuId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-禁用, 1-启用)");
        }
        Sku sku = this.getById(id);
        if (sku == null) {
            throw new BusinessException(ResultCode.SKU_NOT_EXIST);
        }
        sku.setStatus(status);
        sku.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(sku);
        if (updated) {
            spuService.recalculateSpuStockAndPrice(sku.getSpuId());
        }
        return updated;
    }
}
