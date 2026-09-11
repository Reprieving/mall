package com.example.baseboot.module.product.spu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.brand.entity.Brand;
import com.example.baseboot.module.product.brand.mapper.BrandMapper;
import com.example.baseboot.module.product.brand.vo.BrandVO;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.mapper.CategoryMapper;
import com.example.baseboot.module.product.category.vo.CategoryVO;
import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.sku.mapper.SkuMapper;
import com.example.baseboot.module.product.sku.vo.SkuVO;
import com.example.baseboot.module.product.spec.dto.SkuSpecValueItemDTO;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.entity.SpuSpecRelation;
import com.example.baseboot.module.product.spec.mapper.SpecKeyMapper;
import com.example.baseboot.module.product.spec.mapper.SpecValueMapper;
import com.example.baseboot.module.product.spec.mapper.SpuSpecRelationMapper;
import com.example.baseboot.module.product.spec.service.SkuSpecValueService;
import com.example.baseboot.module.product.spec.util.SkuSpecUtils;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;
import com.example.baseboot.module.product.spec.vo.SpecValueVO;
import com.example.baseboot.module.product.spu.dto.SpuCreateDTO;
import com.example.baseboot.module.product.spu.dto.SpuQueryDTO;
import com.example.baseboot.module.product.spu.dto.SpuSpecItemDTO;
import com.example.baseboot.module.product.spu.dto.SpuUpdateDTO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import com.example.baseboot.module.product.spu.service.SpuService;
import com.example.baseboot.module.product.spu.vo.SpuDetailVO;
import com.example.baseboot.module.product.spu.vo.SpuSpecVO;
import com.example.baseboot.module.product.spu.vo.SpuVO;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.mapper.ShopMapper;
import com.example.baseboot.module.shop.vo.ShopVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品 SPU 业务实现类 (基于方案 B 全关系型多表关联及店铺归属)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {

    private final SkuMapper skuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final ShopMapper shopMapper;
    private final SpecKeyMapper specKeyMapper;
    private final SpecValueMapper specValueMapper;
    private final SpuSpecRelationMapper spuSpecRelationMapper;
    private final SkuSpecValueService skuSpecValueService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpuDetailVO createSpu(SpuCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU参数不能为空");
        }

        // 1. 校验所属店铺
        Long shopId = createDTO.getShopId() != null ? createDTO.getShopId() : 1L;
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }

        // 2. 校验分类
        Category category = categoryMapper.selectById(createDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        // 3. 校验品牌
        Brand brand = null;
        if (createDTO.getBrandId() != null && createDTO.getBrandId() > 0) {
            brand = brandMapper.selectById(createDTO.getBrandId());
            if (brand == null) {
                throw new BusinessException(ResultCode.BRAND_NOT_EXIST);
            }
        }

        // 3. 校验并生成 SPU 编码
        String spuCode = createDTO.getSpuCode();
        if (StringUtils.hasText(spuCode)) {
            spuCode = spuCode.trim();
            long count = this.count(new LambdaQueryWrapper<Spu>().eq(Spu::getSpuCode, spuCode));
            if (count > 0) {
                throw new BusinessException(ResultCode.SPU_CODE_EXISTS);
            }
        } else {
            spuCode = generateSpuCode();
        }

        // 4. 校验 SKU 列表
        List<SkuItemDTO> skuDTOList = createDTO.getSkuList();
        if (CollectionUtils.isEmpty(skuDTOList)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "至少需要包含一个SKU信息");
        }

        Set<String> requestSkuCodes = new HashSet<>();
        for (SkuItemDTO skuItem : skuDTOList) {
            if (StringUtils.hasText(skuItem.getSkuCode())) {
                if (!requestSkuCodes.add(skuItem.getSkuCode().trim())) {
                    throw new BusinessException(ResultCode.SKU_CODE_EXISTS, "请求中存在重复的SKU编码: " + skuItem.getSkuCode());
                }
                Long existsCount = skuMapper.selectCount(new LambdaQueryWrapper<Sku>().eq(Sku::getSkuCode, skuItem.getSkuCode().trim()));
                if (existsCount > 0) {
                    throw new BusinessException(ResultCode.SKU_CODE_EXISTS, "SKU编码已存在: " + skuItem.getSkuCode());
                }
            } else {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU编码不能为空");
            }
        }

        // 校验 SKU 列表内部规格组合唯一性
        validateSkuSpecifications(null, skuDTOList);

        // 5. 计算最低售价、最高售价、总库存
        BigDecimal minPrice = skuDTOList.stream()
                .map(SkuItemDTO::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = skuDTOList.stream()
                .map(SkuItemDTO::getPrice)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int totalStock = skuDTOList.stream()
                .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                .sum();

        // 6. 保存 SPU 主表
        int specType = createDTO.getSpecType() != null ? createDTO.getSpecType() : 1;
        Spu spu = Spu.builder()
                .shopId(shopId)
                .name(createDTO.getName().trim())
                .spuCode(spuCode)
                .categoryId(createDTO.getCategoryId())
                .brandId(createDTO.getBrandId())
                .title(createDTO.getTitle())
                .description(createDTO.getDescription())
                .mainPic(createDTO.getMainPic())
                .sliderPics(createDTO.getSliderPics())
                .specType(specType)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .totalStock(totalStock)
                .unit(StringUtils.hasText(createDTO.getUnit()) ? createDTO.getUnit().trim() : "件")
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : 1)
                .sort(createDTO.getSort() != null ? createDTO.getSort() : 0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(spu);
        Long spuId = spu.getId();

        // 7. 处理 SPU 规格选用关联 (pms_spu_spec_relation)
        List<SpuSpecVO> spuSpecVOList = saveSpuSpecRelations(spuId, createDTO.getCategoryId(), specType, createDTO.getSpecList(), createDTO.getSkuList());

        // 8. 保存 SKU 列表及关联关系 (pms_sku & pms_sku_spec_value)
        List<SkuVO> skuVOList = new ArrayList<>();
        for (SkuItemDTO skuDTO : skuDTOList) {
            String specData = skuDTO.getSpecData();
            if (!StringUtils.hasText(specData) && !CollectionUtils.isEmpty(skuDTO.getSpecValues())) {
                try {
                    specData = OBJECT_MAPPER.writeValueAsString(skuDTO.getSpecValues());
                } catch (Exception ignored) {
                }
            }

            Sku sku = Sku.builder()
                    .spuId(spuId)
                    .skuCode(skuDTO.getSkuCode().trim())
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

            skuMapper.insert(sku);

            // 保存多对多绑定表 pms_sku_spec_value
            skuSpecValueService.saveSkuSpecValues(spuId, sku.getId(), skuDTO.getSpecValues());

            SkuVO skuVO = SkuVO.fromEntity(sku);
            skuVO.setSpecValues(skuSpecValueService.listBySkuId(sku.getId()));
            skuVOList.add(skuVO);
        }

        // 9. 组装返回详情 VO
        SpuVO spuVO = SpuVO.fromEntity(spu);
        spuVO.setShopName(shop.getName());
        spuVO.setCategoryName(category.getName());
        spuVO.setBrandName(brand != null ? brand.getName() : null);

        return SpuDetailVO.builder()
                .spuInfo(spuVO)
                .shop(ShopVO.fromEntity(shop))
                .category(CategoryVO.fromEntity(category))
                .brand(BrandVO.fromEntity(brand))
                .specList(spuSpecVOList)
                .skuList(skuVOList)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SpuDetailVO updateSpu(Long id, SpuUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU ID不能为空");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }

        // 1. 校验所属店铺
        Long shopId = updateDTO.getShopId() != null ? updateDTO.getShopId() : spu.getShopId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException(ResultCode.SHOP_NOT_EXIST);
        }
        spu.setShopId(shopId);

        // 2. 校验分类与品牌
        Category category = categoryMapper.selectById(updateDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_EXIST);
        }

        Brand brand = null;
        if (updateDTO.getBrandId() != null && updateDTO.getBrandId() > 0) {
            brand = brandMapper.selectById(updateDTO.getBrandId());
            if (brand == null) {
                throw new BusinessException(ResultCode.BRAND_NOT_EXIST);
            }
        }

        // 2. 校验 SPU 编码
        if (StringUtils.hasText(updateDTO.getSpuCode())) {
            String newCode = updateDTO.getSpuCode().trim();
            if (!newCode.equalsIgnoreCase(spu.getSpuCode())) {
                long count = this.count(new LambdaQueryWrapper<Spu>()
                        .eq(Spu::getSpuCode, newCode)
                        .ne(Spu::getId, id));
                if (count > 0) {
                    throw new BusinessException(ResultCode.SPU_CODE_EXISTS);
                }
                spu.setSpuCode(newCode);
            }
        }

        // 3. 校验 SKU 列表不能为空且规格组合不重复
        List<SkuItemDTO> incomingSkuList = updateDTO.getSkuList();
        if (CollectionUtils.isEmpty(incomingSkuList)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU列表不能为空");
        }
        validateSkuSpecifications(id, incomingSkuList);

        // 4. 重建 SPU 规格选用关联 (pms_spu_spec_relation)
        spuSpecRelationMapper.delete(new LambdaQueryWrapper<SpuSpecRelation>().eq(SpuSpecRelation::getSpuId, id));
        int specType = updateDTO.getSpecType() != null ? updateDTO.getSpecType() : spu.getSpecType();
        List<SpuSpecVO> spuSpecVOList = saveSpuSpecRelations(id, updateDTO.getCategoryId(), specType, updateDTO.getSpecList(), incomingSkuList);

        // 5. 维护 SKU 列表 (更新已有、新增、清理移除的 SKU 与 pms_sku_spec_value)
        List<Sku> existingSkus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
        Map<Long, Sku> existingSkuMap = existingSkus.stream().collect(Collectors.toMap(Sku::getId, s -> s));

        Set<Long> retainSkuIds = new HashSet<>();
        List<SkuVO> resultSkuVOs = new ArrayList<>();

        for (SkuItemDTO skuDTO : incomingSkuList) {
            if (!StringUtils.hasText(skuDTO.getSkuCode())) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "SKU编码不能为空");
            }
            String skuCode = skuDTO.getSkuCode().trim();

            String specData = skuDTO.getSpecData();
            if (!StringUtils.hasText(specData) && !CollectionUtils.isEmpty(skuDTO.getSpecValues())) {
                try {
                    specData = OBJECT_MAPPER.writeValueAsString(skuDTO.getSpecValues());
                } catch (Exception ignored) {
                }
            }

            if (skuDTO.getId() != null && existingSkuMap.containsKey(skuDTO.getId())) {
                // 更新现有 SKU
                Sku sku = existingSkuMap.get(skuDTO.getId());
                if (!skuCode.equalsIgnoreCase(sku.getSkuCode())) {
                    Long existsCount = skuMapper.selectCount(new LambdaQueryWrapper<Sku>()
                            .eq(Sku::getSkuCode, skuCode)
                            .ne(Sku::getId, sku.getId()));
                    if (existsCount > 0) {
                        throw new BusinessException(ResultCode.SKU_CODE_EXISTS, "SKU编码已存在: " + skuCode);
                    }
                    sku.setSkuCode(skuCode);
                }
                sku.setName(skuDTO.getName().trim());
                sku.setPic(skuDTO.getPic());
                sku.setPrice(skuDTO.getPrice());
                sku.setOriginalPrice(skuDTO.getOriginalPrice());
                sku.setCostPrice(skuDTO.getCostPrice());
                sku.setStock(skuDTO.getStock());
                sku.setWeight(skuDTO.getWeight() != null ? skuDTO.getWeight() : BigDecimal.ZERO);
                sku.setVolume(skuDTO.getVolume() != null ? skuDTO.getVolume() : BigDecimal.ZERO);
                sku.setSpecData(specData);
                if (skuDTO.getStatus() != null) {
                    sku.setStatus(skuDTO.getStatus());
                }
                sku.setUpdateTime(LocalDateTime.now());
                skuMapper.updateById(sku);

                // 更新 SKU 规格多对多绑定表
                skuSpecValueService.saveSkuSpecValues(id, sku.getId(), skuDTO.getSpecValues());

                retainSkuIds.add(sku.getId());
                SkuVO skuVO = SkuVO.fromEntity(sku);
                skuVO.setSpecValues(skuSpecValueService.listBySkuId(sku.getId()));
                resultSkuVOs.add(skuVO);
            } else {
                // 新建 SKU
                Long existsCount = skuMapper.selectCount(new LambdaQueryWrapper<Sku>().eq(Sku::getSkuCode, skuCode));
                if (existsCount > 0) {
                    throw new BusinessException(ResultCode.SKU_CODE_EXISTS, "SKU编码已存在: " + skuCode);
                }
                Sku newSku = Sku.builder()
                        .spuId(id)
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
                skuMapper.insert(newSku);

                skuSpecValueService.saveSkuSpecValues(id, newSku.getId(), skuDTO.getSpecValues());

                retainSkuIds.add(newSku.getId());
                SkuVO skuVO = SkuVO.fromEntity(newSku);
                skuVO.setSpecValues(skuSpecValueService.listBySkuId(newSku.getId()));
                resultSkuVOs.add(skuVO);
            }
        }

        // 删除本次更新中未保留的旧 SKU 及其规格绑定
        for (Sku oldSku : existingSkus) {
            if (!retainSkuIds.contains(oldSku.getId())) {
                skuSpecValueService.deleteBySkuId(oldSku.getId());
                skuMapper.deleteById(oldSku.getId());
            }
        }

        // 5. 重新计算价格与库存
        BigDecimal minPrice = incomingSkuList.stream()
                .map(SkuItemDTO::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = incomingSkuList.stream()
                .map(SkuItemDTO::getPrice)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int totalStock = incomingSkuList.stream()
                .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                .sum();

        // 6. 更新 SPU 信息
        spu.setName(updateDTO.getName().trim());
        spu.setCategoryId(updateDTO.getCategoryId());
        spu.setBrandId(updateDTO.getBrandId());
        spu.setTitle(updateDTO.getTitle());
        spu.setDescription(updateDTO.getDescription());
        spu.setMainPic(updateDTO.getMainPic());
        spu.setSliderPics(updateDTO.getSliderPics());
        spu.setSpecType(specType);
        spu.setMinPrice(minPrice);
        spu.setMaxPrice(maxPrice);
        spu.setTotalStock(totalStock);
        if (StringUtils.hasText(updateDTO.getUnit())) {
            spu.setUnit(updateDTO.getUnit().trim());
        }
        if (updateDTO.getSort() != null) {
            spu.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            spu.setStatus(updateDTO.getStatus());
        }
        spu.setUpdateTime(LocalDateTime.now());
        this.updateById(spu);

        SpuVO spuVO = SpuVO.fromEntity(spu);
        spuVO.setShopName(shop != null ? shop.getName() : null);
        spuVO.setCategoryName(category.getName());
        spuVO.setBrandName(brand != null ? brand.getName() : null);

        return SpuDetailVO.builder()
                .spuInfo(spuVO)
                .shop(ShopVO.fromEntity(shop))
                .category(CategoryVO.fromEntity(category))
                .brand(BrandVO.fromEntity(brand))
                .specList(spuSpecVOList)
                .skuList(resultSkuVOs)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpu(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU ID不能为空");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }

        // 级联清理 SKU 规格关系、SKU 表、SPU 规格选用表
        skuSpecValueService.deleteBySpuId(id);
        skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
        spuSpecRelationMapper.delete(new LambdaQueryWrapper<SpuSpecRelation>().eq(SpuSpecRelation::getSpuId, id));

        return this.removeById(id);
    }

    @Override
    public SpuDetailVO getSpuDetail(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU ID不能为空");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }

        Shop shop = spu.getShopId() != null ? shopMapper.selectById(spu.getShopId()) : null;
        Category category = categoryMapper.selectById(spu.getCategoryId());
        Brand brand = spu.getBrandId() != null ? brandMapper.selectById(spu.getBrandId()) : null;

        // 加载 SPU 选用的规格维度列表
        List<SpuSpecRelation> relations = spuSpecRelationMapper.selectList(new LambdaQueryWrapper<SpuSpecRelation>()
                .eq(SpuSpecRelation::getSpuId, id)
                .orderByAsc(SpuSpecRelation::getId));

        List<SpuSpecVO> spuSpecVOList = assembleSpuSpecs(relations, id);

        // 加载旗下 SKU 列表及规格绑定明细
        List<Sku> skuEntities = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, id)
                .orderByAsc(Sku::getId));

        List<Long> skuIds = skuEntities.stream().map(Sku::getId).collect(Collectors.toList());
        Map<Long, List<SkuSpecValueVO>> skuSpecMap = skuSpecValueService.mapBySkuIds(skuIds);

        List<SkuVO> skuVOList = skuEntities.stream().map(s -> {
            SkuVO vo = SkuVO.fromEntity(s);
            vo.setSpecValues(skuSpecMap.getOrDefault(s.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

        SpuVO spuVO = SpuVO.fromEntity(spu);
        if (shop != null) {
            spuVO.setShopName(shop.getName());
        }
        if (category != null) {
            spuVO.setCategoryName(category.getName());
        }
        if (brand != null) {
            spuVO.setBrandName(brand.getName());
        }

        return SpuDetailVO.builder()
                .spuInfo(spuVO)
                .shop(ShopVO.fromEntity(shop))
                .category(CategoryVO.fromEntity(category))
                .brand(BrandVO.fromEntity(brand))
                .specList(spuSpecVOList)
                .skuList(skuVOList)
                .build();
    }

    @Override
    public CommonPage<SpuVO> pageSpu(SpuQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new SpuQueryDTO();
        }
        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Spu> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String kw = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(Spu::getName, kw).or().like(Spu::getTitle, kw));
        }
        if (StringUtils.hasText(queryDTO.getSpuCode())) {
            wrapper.eq(Spu::getSpuCode, queryDTO.getSpuCode().trim());
        }
        if (queryDTO.getShopId() != null) {
            wrapper.eq(Spu::getShopId, queryDTO.getShopId());
        }
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Spu::getCategoryId, queryDTO.getCategoryId());
        }
        if (queryDTO.getBrandId() != null) {
            wrapper.eq(Spu::getBrandId, queryDTO.getBrandId());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Spu::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByAsc(Spu::getSort).orderByDesc(Spu::getId);
        Page<Spu> spuPage = this.page(page, wrapper);

        if (CollectionUtils.isEmpty(spuPage.getRecords())) {
            return CommonPage.restPage(spuPage, Collections.emptyList());
        }

        Set<Long> shopIds = spuPage.getRecords().stream().map(Spu::getShopId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = spuPage.getRecords().stream().map(Spu::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> brandIds = spuPage.getRecords().stream().map(Spu::getBrandId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> shopNameMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            List<Shop> shops = shopMapper.selectBatchIds(shopIds);
            if (shops != null) {
                shopNameMap = shops.stream().collect(Collectors.toMap(Shop::getId, Shop::getName, (a, b) -> a));
            }
        }

        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(categoryIds);
            if (categories != null) {
                categoryNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
            }
        }

        Map<Long, String> brandNameMap = new HashMap<>();
        if (!brandIds.isEmpty()) {
            List<Brand> brands = brandMapper.selectBatchIds(brandIds);
            if (brands != null) {
                brandNameMap = brands.stream().collect(Collectors.toMap(Brand::getId, Brand::getName, (a, b) -> a));
            }
        }

        Map<Long, String> finalShopNameMap = shopNameMap;
        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        Map<Long, String> finalBrandNameMap = brandNameMap;

        List<SpuVO> voList = spuPage.getRecords().stream().map(s -> {
            SpuVO vo = SpuVO.fromEntity(s);
            vo.setShopName(finalShopNameMap.get(s.getShopId()));
            vo.setCategoryName(finalCategoryNameMap.get(s.getCategoryId()));
            vo.setBrandName(finalBrandNameMap.get(s.getBrandId()));
            return vo;
        }).collect(Collectors.toList());

        return CommonPage.restPage(spuPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "SPU ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-下架, 1-上架)");
        }
        Spu spu = this.getById(id);
        if (spu == null) {
            throw new BusinessException(ResultCode.SPU_NOT_EXIST);
        }
        spu.setStatus(status);
        spu.setUpdateTime(LocalDateTime.now());
        return this.updateById(spu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<Long> ids, Integer status) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "ID列表不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-下架, 1-上架)");
        }
        return this.lambdaUpdate()
                .in(Spu::getId, ids)
                .set(Spu::getStatus, status)
                .set(Spu::getUpdateTime, LocalDateTime.now())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateSpuStockAndPrice(Long spuId) {
        if (spuId == null) {
            return;
        }
        Spu spu = this.getById(spuId);
        if (spu == null) {
            return;
        }

        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .eq(Sku::getStatus, 1));

        if (CollectionUtils.isEmpty(skus)) {
            skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId));
        }

        if (CollectionUtils.isEmpty(skus)) {
            spu.setMinPrice(BigDecimal.ZERO);
            spu.setMaxPrice(BigDecimal.ZERO);
            spu.setTotalStock(0);
        } else {
            BigDecimal min = skus.stream().map(Sku::getPrice).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = skus.stream().map(Sku::getPrice).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            int total = skus.stream().mapToInt(s -> s.getStock() != null ? s.getStock() : 0).sum();
            spu.setMinPrice(min);
            spu.setMaxPrice(max);
            spu.setTotalStock(total);
        }

        spu.setUpdateTime(LocalDateTime.now());
        this.updateById(spu);
    }

    /**
     * 保存 SPU 选用的规格维度与规格值列表 (融合 specList 与 skuList)
     */
    private List<SpuSpecVO> saveSpuSpecRelations(Long spuId, Long categoryId, int specType, List<SpuSpecItemDTO> specList, List<SkuItemDTO> skuList) {
        List<SpuSpecVO> resultList = new ArrayList<>();
        if (spuId == null) {
            return resultList;
        }

        Map<Long, KeyHolder> keyHolderMap = new LinkedHashMap<>();

        // 1. 采集顶层规格清单 specList
        if (!CollectionUtils.isEmpty(specList)) {
            for (SpuSpecItemDTO item : specList) {
                if (item == null) continue;
                Long keyId = resolveOrCreateSpecKey(item.getSpecKeyId(), item.getSpecName(), categoryId);
                if (keyId == null) continue;

                String keyName = item.getSpecName();
                if (!StringUtils.hasText(keyName)) {
                    SpecKey k = specKeyMapper.selectById(keyId);
                    if (k != null) keyName = k.getName();
                }

                KeyHolder holder = keyHolderMap.computeIfAbsent(keyId, k -> new KeyHolder(k, ""));
                if (StringUtils.hasText(keyName) && !StringUtils.hasText(holder.keyName)) {
                    holder.keyName = keyName;
                }

                if (!CollectionUtils.isEmpty(item.getSpecValueIds())) {
                    for (Long valId : item.getSpecValueIds()) {
                        if (valId == null) continue;
                        SpecValue valEntity = specValueMapper.selectById(valId);
                        if (valEntity != null && StringUtils.hasText(valEntity.getValue())) {
                            String vStr = valEntity.getValue().trim();
                            holder.values.add(vStr);
                            holder.valueToId.put(vStr, valEntity.getId());
                        }
                    }
                }

                if (!CollectionUtils.isEmpty(item.getSpecValues())) {
                    for (String valStr : item.getSpecValues()) {
                        if (StringUtils.hasText(valStr)) {
                            holder.values.add(valStr.trim());
                        }
                    }
                }
            }
        }

        // 2. 补充采集 SKU 矩阵中的规格项 (保障 SKU 中出现的规格全部纳入 SPU 规格矩阵)
        if (!CollectionUtils.isEmpty(skuList)) {
            for (SkuItemDTO sku : skuList) {
                if (sku == null) continue;
                List<SkuSpecValueItemDTO> specValues = sku.getSpecValues();
                if (CollectionUtils.isEmpty(specValues) && StringUtils.hasText(sku.getSpecData())) {
                    try {
                        specValues = OBJECT_MAPPER.readValue(
                                sku.getSpecData(),
                                OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, SkuSpecValueItemDTO.class)
                        );
                    } catch (Exception ignored) {}
                }
                if (!CollectionUtils.isEmpty(specValues)) {
                    for (SkuSpecValueItemDTO sv : specValues) {
                        if (sv == null) continue;
                        Long keyId = resolveOrCreateSpecKey(sv.getSpecKeyId(), sv.getSpecKeyName(), categoryId);
                        if (keyId == null) continue;

                        String keyName = sv.getSpecKeyName();
                        if (!StringUtils.hasText(keyName)) {
                            SpecKey k = specKeyMapper.selectById(keyId);
                            if (k != null) keyName = k.getName();
                        }

                        KeyHolder holder = keyHolderMap.computeIfAbsent(keyId, k -> new KeyHolder(k, ""));
                        if (StringUtils.hasText(keyName) && !StringUtils.hasText(holder.keyName)) {
                            holder.keyName = keyName;
                        }

                        String valStr = sv.getSpecValue();
                        Long valId = sv.getSpecValueId();
                        if (StringUtils.hasText(valStr)) {
                            String trimmed = valStr.trim();
                            holder.values.add(trimmed);
                            if (valId != null) {
                                holder.valueToId.put(trimmed, valId);
                            }
                        } else if (valId != null) {
                            SpecValue valEntity = specValueMapper.selectById(valId);
                            if (valEntity != null && StringUtils.hasText(valEntity.getValue())) {
                                String trimmed = valEntity.getValue().trim();
                                holder.values.add(trimmed);
                                holder.valueToId.put(trimmed, valEntity.getId());
                            }
                        }
                    }
                }
            }
        }

        if (keyHolderMap.isEmpty()) {
            return resultList;
        }

        // 3. 落库关联关系与构建结果
        for (KeyHolder holder : keyHolderMap.values()) {
            Long keyId = holder.keyId;
            String keyName = holder.keyName;
            if (!StringUtils.hasText(keyName)) {
                SpecKey k = specKeyMapper.selectById(keyId);
                keyName = k != null ? k.getName() : "";
            }

            List<SpecValueVO> valueVOs = new ArrayList<>();
            for (String valStr : holder.values) {
                Long candidateId = holder.valueToId.get(valStr);
                SpecValue valEntity = null;
                if (candidateId != null) {
                    SpecValue existing = specValueMapper.selectById(candidateId);
                    if (existing != null && Objects.equals(existing.getSpecKeyId(), keyId)
                            && valStr.equalsIgnoreCase(existing.getValue().trim())) {
                        valEntity = existing;
                    }
                }
                if (valEntity == null) {
                    valEntity = specValueMapper.selectOne(new LambdaQueryWrapper<SpecValue>()
                            .eq(SpecValue::getSpecKeyId, keyId)
                            .eq(SpecValue::getValue, valStr)
                            .last("LIMIT 1"));
                }
                if (valEntity == null) {
                    valEntity = SpecValue.builder()
                            .specKeyId(keyId)
                            .value(valStr)
                            .sort(0)
                            .status(1)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    specValueMapper.insert(valEntity);
                }

                Long valId = valEntity.getId();
                Long relCount = spuSpecRelationMapper.selectCount(new LambdaQueryWrapper<SpuSpecRelation>()
                        .eq(SpuSpecRelation::getSpuId, spuId)
                        .eq(SpuSpecRelation::getSpecKeyId, keyId)
                        .eq(SpuSpecRelation::getSpecValueId, valId));
                if (relCount == 0) {
                    SpuSpecRelation relation = SpuSpecRelation.builder()
                            .spuId(spuId)
                            .specKeyId(keyId)
                            .specValueId(valId)
                            .createTime(LocalDateTime.now())
                            .build();
                    spuSpecRelationMapper.insert(relation);
                }

                valueVOs.add(SpecValueVO.fromEntity(valEntity));
            }

            valueVOs.sort(Comparator.comparing(SpecValueVO::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(SpecValueVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));

            resultList.add(SpuSpecVO.builder()
                    .specKeyId(keyId)
                    .specName(keyName)
                    .values(valueVOs)
                    .build());
        }

        return resultList;
    }

    /**
     * 将 SPU 关联表数据组装为结构化规格列表 (支持从 SKU 规格绑定及 SKU specData 动态融合与自愈)
     */
    private List<SpuSpecVO> assembleSpuSpecs(List<SpuSpecRelation> relations, Long spuId) {
        Map<Long, KeyHolder> keyHolderMap = new LinkedHashMap<>();

        // 1. 先从已有的 pms_spu_spec_relation 中提取规格信息
        if (!CollectionUtils.isEmpty(relations)) {
            Set<Long> keyIds = relations.stream().map(SpuSpecRelation::getSpecKeyId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<Long> valueIds = relations.stream().map(SpuSpecRelation::getSpecValueId).filter(Objects::nonNull).collect(Collectors.toSet());

            Map<Long, SpecKey> keyMap = new HashMap<>();
            if (!keyIds.isEmpty()) {
                List<SpecKey> keys = specKeyMapper.selectBatchIds(keyIds);
                if (keys != null) {
                    keyMap = keys.stream().collect(Collectors.toMap(SpecKey::getId, k -> k, (a, b) -> a));
                }
            }

            Map<Long, SpecValue> valMap = new HashMap<>();
            if (!valueIds.isEmpty()) {
                List<SpecValue> vals = specValueMapper.selectBatchIds(valueIds);
                if (vals != null) {
                    valMap = vals.stream().collect(Collectors.toMap(SpecValue::getId, v -> v, (a, b) -> a));
                }
            }

            for (SpuSpecRelation rel : relations) {
                Long keyId = rel.getSpecKeyId();
                Long valId = rel.getSpecValueId();
                if (keyId == null || valId == null) {
                    continue;
                }
                SpecKey key = keyMap.get(keyId);
                String keyName = key != null ? key.getName() : "";
                KeyHolder holder = keyHolderMap.computeIfAbsent(keyId, k -> new KeyHolder(k, ""));
                if (StringUtils.hasText(keyName) && !StringUtils.hasText(holder.keyName)) {
                    holder.keyName = keyName;
                }

                SpecValue val = valMap.get(valId);
                if (val != null && StringUtils.hasText(val.getValue())) {
                    String vStr = val.getValue().trim();
                    holder.values.add(vStr);
                    holder.valueToId.put(vStr, val.getId());
                }
            }
        }

        // 2. 从 SPU 下的 SKU 规格多对多表 (pms_sku_spec_value) 及 SKU 表 (specData) 中补充和融合规格 (自愈缺失与冲突数据)
        if (spuId != null) {
            List<SkuSpecValue> skuSpecValues = skuSpecValueService.list(new LambdaQueryWrapper<SkuSpecValue>()
                    .eq(SkuSpecValue::getSpuId, spuId)
                    .orderByAsc(SkuSpecValue::getId));

            if (!CollectionUtils.isEmpty(skuSpecValues)) {
                for (SkuSpecValue ssv : skuSpecValues) {
                    if (ssv == null) continue;
                    Long keyId = ssv.getSpecKeyId();
                    String keyName = ssv.getSpecKeyName();
                    if (keyId == null && StringUtils.hasText(keyName)) {
                        keyId = resolveOrCreateSpecKey(null, keyName, null);
                    }
                    if (keyId == null) continue;

                    KeyHolder holder = keyHolderMap.computeIfAbsent(keyId, k -> new KeyHolder(k, ""));
                    if (StringUtils.hasText(keyName) && !StringUtils.hasText(holder.keyName)) {
                        holder.keyName = keyName;
                    }

                    String valStr = ssv.getSpecValue();
                    Long valId = ssv.getSpecValueId();
                    if (StringUtils.hasText(valStr)) {
                        String trimmed = valStr.trim();
                        holder.values.add(trimmed);
                        if (valId != null) {
                            holder.valueToId.put(trimmed, valId);
                        }
                    } else if (valId != null) {
                        SpecValue valEntity = specValueMapper.selectById(valId);
                        if (valEntity != null && StringUtils.hasText(valEntity.getValue())) {
                            String trimmed = valEntity.getValue().trim();
                            holder.values.add(trimmed);
                            holder.valueToId.put(trimmed, valEntity.getId());
                        }
                    }
                }
            }

            // 补充从 SPU 下的 SKU 表的 specData 提取规格 (若 pms_sku_spec_value 遗漏)
            List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                    .eq(Sku::getSpuId, spuId)
                    .orderByAsc(Sku::getId));
            if (!CollectionUtils.isEmpty(skus)) {
                for (Sku sku : skus) {
                    if (StringUtils.hasText(sku.getSpecData())) {
                        try {
                            List<SkuSpecValueItemDTO> jsonItems = OBJECT_MAPPER.readValue(
                                    sku.getSpecData(),
                                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, SkuSpecValueItemDTO.class)
                            );
                            if (!CollectionUtils.isEmpty(jsonItems)) {
                                for (SkuSpecValueItemDTO sv : jsonItems) {
                                    if (sv == null) continue;
                                    Long keyId = sv.getSpecKeyId();
                                    String keyName = sv.getSpecKeyName();
                                    if (keyId == null && StringUtils.hasText(keyName)) {
                                        keyId = resolveOrCreateSpecKey(null, keyName, null);
                                    }
                                    if (keyId == null) continue;

                                    KeyHolder holder = keyHolderMap.computeIfAbsent(keyId, k -> new KeyHolder(k, ""));
                                    if (StringUtils.hasText(keyName) && !StringUtils.hasText(holder.keyName)) {
                                        holder.keyName = keyName;
                                    }

                                    String valStr = sv.getSpecValue();
                                    Long valId = sv.getSpecValueId();
                                    if (StringUtils.hasText(valStr)) {
                                        String trimmed = valStr.trim();
                                        holder.values.add(trimmed);
                                        if (valId != null) {
                                            holder.valueToId.put(trimmed, valId);
                                        }
                                    } else if (valId != null) {
                                        SpecValue valEntity = specValueMapper.selectById(valId);
                                        if (valEntity != null && StringUtils.hasText(valEntity.getValue())) {
                                            String trimmed = valEntity.getValue().trim();
                                            holder.values.add(trimmed);
                                            holder.valueToId.put(trimmed, valEntity.getId());
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        if (keyHolderMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 构建结构化规格列表，并自愈 pms_spu_spec_relation 和 pms_sku_spec_value 中的异常数据
        List<SpuSpecVO> resultList = new ArrayList<>();
        Map<String, Long> textToResolvedValId = new HashMap<>();

        for (KeyHolder holder : keyHolderMap.values()) {
            Long keyId = holder.keyId;
            String keyName = holder.keyName;
            if (!StringUtils.hasText(keyName)) {
                SpecKey k = specKeyMapper.selectById(keyId);
                keyName = k != null ? k.getName() : "";
            }

            List<SpecValueVO> valueVOs = new ArrayList<>();
            for (String valStr : holder.values) {
                Long candidateId = holder.valueToId.get(valStr);
                SpecValue valEntity = null;
                if (candidateId != null) {
                    SpecValue existing = specValueMapper.selectById(candidateId);
                    if (existing != null && Objects.equals(existing.getSpecKeyId(), keyId)
                            && valStr.equalsIgnoreCase(existing.getValue().trim())) {
                        valEntity = existing;
                    }
                }
                if (valEntity == null) {
                    valEntity = specValueMapper.selectOne(new LambdaQueryWrapper<SpecValue>()
                            .eq(SpecValue::getSpecKeyId, keyId)
                            .eq(SpecValue::getValue, valStr)
                            .last("LIMIT 1"));
                }
                if (valEntity == null) {
                    valEntity = SpecValue.builder()
                            .specKeyId(keyId)
                            .value(valStr)
                            .sort(0)
                            .status(1)
                            .createTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .build();
                    specValueMapper.insert(valEntity);
                }

                Long realValId = valEntity.getId();
                textToResolvedValId.put(keyId + "_" + valStr.toLowerCase(), realValId);

                // 自愈补全 pms_spu_spec_relation
                if (spuId != null) {
                    Long relCount = spuSpecRelationMapper.selectCount(new LambdaQueryWrapper<SpuSpecRelation>()
                            .eq(SpuSpecRelation::getSpuId, spuId)
                            .eq(SpuSpecRelation::getSpecKeyId, keyId)
                            .eq(SpuSpecRelation::getSpecValueId, realValId));
                    if (relCount == 0) {
                        SpuSpecRelation rel = SpuSpecRelation.builder()
                                .spuId(spuId)
                                .specKeyId(keyId)
                                .specValueId(realValId)
                                .createTime(LocalDateTime.now())
                                .build();
                        spuSpecRelationMapper.insert(rel);
                    }
                }

                valueVOs.add(SpecValueVO.fromEntity(valEntity));
            }

            // 按 sort 和 id 排序
            valueVOs.sort(Comparator.comparing(SpecValueVO::getSort, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(SpecValueVO::getId, Comparator.nullsLast(Comparator.naturalOrder())));

            resultList.add(SpuSpecVO.builder()
                    .specKeyId(keyId)
                    .specName(keyName)
                    .values(valueVOs)
                    .build());
        }

        // 4. 自愈修正 pms_sku_spec_value 中 ID 错位的记录
        if (spuId != null && !textToResolvedValId.isEmpty()) {
            List<SkuSpecValue> skuSpecValues = skuSpecValueService.list(new LambdaQueryWrapper<SkuSpecValue>()
                    .eq(SkuSpecValue::getSpuId, spuId));
            if (!CollectionUtils.isEmpty(skuSpecValues)) {
                for (SkuSpecValue ssv : skuSpecValues) {
                    if (ssv.getSpecKeyId() != null && StringUtils.hasText(ssv.getSpecValue())) {
                        String key = ssv.getSpecKeyId() + "_" + ssv.getSpecValue().trim().toLowerCase();
                        Long correctValId = textToResolvedValId.get(key);
                        if (correctValId != null && !correctValId.equals(ssv.getSpecValueId())) {
                            ssv.setSpecValueId(correctValId);
                            skuSpecValueService.updateById(ssv);
                        }
                    }
                }
            }
        }

        return resultList;
    }

    private Long resolveOrCreateSpecKey(Long keyId, String keyName, Long categoryId) {
        if (keyId != null) {
            SpecKey key = specKeyMapper.selectById(keyId);
            if (key != null) {
                return key.getId();
            }
        }
        if (StringUtils.hasText(keyName)) {
            String trimmedName = keyName.trim();
            SpecKey existKey = specKeyMapper.selectOne(new LambdaQueryWrapper<SpecKey>()
                    .eq(categoryId != null && categoryId > 0, SpecKey::getCategoryId, categoryId)
                    .eq(SpecKey::getName, trimmedName)
                    .last("LIMIT 1"));
            if (existKey == null) {
                existKey = specKeyMapper.selectOne(new LambdaQueryWrapper<SpecKey>()
                        .eq(SpecKey::getCategoryId, 0)
                        .eq(SpecKey::getName, trimmedName)
                        .last("LIMIT 1"));
            }
            if (existKey == null) {
                existKey = SpecKey.builder()
                        .categoryId(categoryId != null ? categoryId : 0L)
                        .name(trimmedName)
                        .sort(0)
                        .status(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                specKeyMapper.insert(existKey);
            }
            return existKey.getId();
        }
        return null;
    }

    private static class KeyHolder {
        Long keyId;
        String keyName;
        LinkedHashSet<String> values = new LinkedHashSet<>();
        Map<String, Long> valueToId = new HashMap<>();

        KeyHolder(Long keyId, String keyName) {
            this.keyId = keyId;
            this.keyName = keyName;
        }
    }

    /**
     * 校验 SKU 列表中的规格组合唯一性 (同一 SPU 下不得存在重复规格维度的 SKU)
     */
    private void validateSkuSpecifications(Long spuId, List<SkuItemDTO> skuList) {
        if (CollectionUtils.isEmpty(skuList)) {
            return;
        }

        // 1. 校验请求内部各 SKU 规格组合是否唯一
        Map<String, SkuItemDTO> requestSpecMap = new LinkedHashMap<>();
        Set<String> requestSkuCodes = new HashSet<>();

        for (SkuItemDTO skuDTO : skuList) {
            if (skuDTO == null) continue;

            if (StringUtils.hasText(skuDTO.getSkuCode())) {
                String code = skuDTO.getSkuCode().trim().toLowerCase();
                if (!requestSkuCodes.add(code)) {
                    throw new BusinessException(ResultCode.SKU_CODE_EXISTS, "请求中存在重复的SKU编码: " + skuDTO.getSkuCode());
                }
            }

            SkuSpecUtils.SpecSignature sig = SkuSpecUtils.extractSignature(
                    skuDTO.getSpecValues(),
                    skuDTO.getSpecData(),
                    specKeyMapper,
                    specValueMapper
            );
            if (sig != null && !sig.isEmpty()) {
                if (requestSpecMap.containsKey(sig.getSignature())) {
                    SkuItemDTO firstSku = requestSpecMap.get(sig.getSignature());
                    throw new BusinessException(ResultCode.VALIDATE_FAILED,
                            "SKU规格已存在或重复，不允许提交相同规格: " + sig.getReadableDesc()
                                    + (StringUtils.hasText(firstSku.getSkuCode()) ? " (与SKU " + firstSku.getSkuCode() + " 冲突)" : ""));
                }
                requestSpecMap.put(sig.getSignature(), skuDTO);
            }
        }

        // 2. 若是修改 SPU (spuId != null)，还需要对比库中已有且未被本次更新覆盖的旧 SKU 规格
        if (spuId != null && !requestSpecMap.isEmpty()) {
            List<Sku> dbSkus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, spuId));
            if (!CollectionUtils.isEmpty(dbSkus)) {
                // 找到请求中包含的所有已有 SKU ID
                Set<Long> updatingSkuIds = skuList.stream()
                        .map(SkuItemDTO::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                // 筛选出库中未在本次请求中被覆盖或更新的旧 SKU
                List<Sku> unupdatedDbSkus = dbSkus.stream()
                        .filter(dbSku -> !updatingSkuIds.contains(dbSku.getId()))
                        .collect(Collectors.toList());

                if (!unupdatedDbSkus.isEmpty()) {
                    List<Long> unupdatedDbSkuIds = unupdatedDbSkus.stream().map(Sku::getId).collect(Collectors.toList());
                    Map<Long, List<SkuSpecValueVO>> dbSkuSpecMap = skuSpecValueService.mapBySkuIds(unupdatedDbSkuIds);
                    for (Map.Entry<String, SkuItemDTO> entry : requestSpecMap.entrySet()) {
                        SkuSpecUtils.SpecSignature reqSig = SkuSpecUtils.extractSignature(
                                entry.getValue().getSpecValues(),
                                entry.getValue().getSpecData(),
                                specKeyMapper,
                                specValueMapper
                        );
                        SkuSpecUtils.checkDuplicateSpecWithExisting(reqSig, unupdatedDbSkus, dbSkuSpecMap, specKeyMapper, specValueMapper);
                    }
                }
            }
        }
    }

    private String generateSpuCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        return "SPU" + timestamp + random;
    }
}
