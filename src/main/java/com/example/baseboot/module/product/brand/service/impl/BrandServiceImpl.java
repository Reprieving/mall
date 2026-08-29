package com.example.baseboot.module.product.brand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.brand.dto.BrandCreateDTO;
import com.example.baseboot.module.product.brand.dto.BrandQueryDTO;
import com.example.baseboot.module.product.brand.dto.BrandUpdateDTO;
import com.example.baseboot.module.product.brand.entity.Brand;
import com.example.baseboot.module.product.brand.mapper.BrandMapper;
import com.example.baseboot.module.product.brand.service.BrandService;
import com.example.baseboot.module.product.brand.vo.BrandVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品品牌业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {

    @Lazy
    private final SpuMapper spuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BrandVO createBrand(BrandCreateDTO createDTO) {
        if (createDTO == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "品牌参数不能为空");
        }

        String brandName = createDTO.getName().trim();
        long count = this.count(new LambdaQueryWrapper<Brand>().eq(Brand::getName, brandName));
        if (count > 0) {
            throw new BusinessException(ResultCode.BRAND_NAME_EXISTS);
        }

        String firstLetter = null;
        if (StringUtils.hasText(createDTO.getFirstLetter())) {
            firstLetter = createDTO.getFirstLetter().trim().toUpperCase();
        }

        Brand brand = Brand.builder()
                .name(brandName)
                .logo(createDTO.getLogo())
                .description(createDTO.getDescription())
                .firstLetter(firstLetter)
                .sort(createDTO.getSort() != null ? createDTO.getSort() : 0)
                .status(createDTO.getStatus() != null ? createDTO.getStatus() : 1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        this.save(brand);
        return BrandVO.fromEntity(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BrandVO updateBrand(Long id, BrandUpdateDTO updateDTO) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "品牌ID不能为空");
        }
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException(ResultCode.BRAND_NOT_EXIST);
        }

        if (StringUtils.hasText(updateDTO.getName())) {
            String newName = updateDTO.getName().trim();
            if (!newName.equalsIgnoreCase(brand.getName())) {
                long count = this.count(new LambdaQueryWrapper<Brand>()
                        .eq(Brand::getName, newName)
                        .ne(Brand::getId, id));
                if (count > 0) {
                    throw new BusinessException(ResultCode.BRAND_NAME_EXISTS);
                }
                brand.setName(newName);
            }
        }

        if (updateDTO.getLogo() != null) {
            brand.setLogo(updateDTO.getLogo());
        }
        if (updateDTO.getDescription() != null) {
            brand.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getFirstLetter() != null) {
            brand.setFirstLetter(StringUtils.hasText(updateDTO.getFirstLetter()) ? updateDTO.getFirstLetter().trim().toUpperCase() : null);
        }
        if (updateDTO.getSort() != null) {
            brand.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            brand.setStatus(updateDTO.getStatus());
        }
        brand.setUpdateTime(LocalDateTime.now());

        this.updateById(brand);
        return BrandVO.fromEntity(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBrand(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "品牌ID不能为空");
        }
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException(ResultCode.BRAND_NOT_EXIST);
        }

        // 检查是否有挂载的 SPU
        if (spuMapper != null) {
            long spuCount = spuMapper.selectCount(new LambdaQueryWrapper<Spu>().eq(Spu::getBrandId, id));
            if (spuCount > 0) {
                throw new BusinessException(ResultCode.BRAND_HAS_PRODUCTS);
            }
        }

        return this.removeById(id);
    }

    @Override
    public BrandVO getBrandById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "品牌ID不能为空");
        }
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException(ResultCode.BRAND_NOT_EXIST);
        }
        return BrandVO.fromEntity(brand);
    }

    @Override
    public CommonPage<BrandVO> pageBrands(BrandQueryDTO queryDTO) {
        if (queryDTO == null) {
            queryDTO = new BrandQueryDTO();
        }
        long pageNum = queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1L;
        long pageSize = queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10L;

        Page<Brand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Brand::getName, queryDTO.getName().trim());
        }
        if (StringUtils.hasText(queryDTO.getFirstLetter())) {
            wrapper.eq(Brand::getFirstLetter, queryDTO.getFirstLetter().trim().toUpperCase());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Brand::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByAsc(Brand::getSort).orderByDesc(Brand::getId);

        Page<Brand> brandPage = this.page(page, wrapper);
        List<BrandVO> voList = brandPage.getRecords().stream()
                .map(BrandVO::fromEntity)
                .collect(Collectors.toList());

        return CommonPage.restPage(brandPage, voList);
    }

    @Override
    public List<BrandVO> listAllBrands() {
        List<Brand> list = this.list(new LambdaQueryWrapper<Brand>()
                .eq(Brand::getStatus, 1)
                .orderByAsc(Brand::getSort)
                .orderByAsc(Brand::getFirstLetter)
                .orderByAsc(Brand::getId));

        return list.stream().map(BrandVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "品牌ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "状态值非法 (0-禁用, 1-启用)");
        }
        Brand brand = this.getById(id);
        if (brand == null) {
            throw new BusinessException(ResultCode.BRAND_NOT_EXIST);
        }
        brand.setStatus(status);
        brand.setUpdateTime(LocalDateTime.now());
        return this.updateById(brand);
    }
}
