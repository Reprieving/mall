package com.example.baseboot.module.product.brand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.product.brand.dto.BrandCreateDTO;
import com.example.baseboot.module.product.brand.dto.BrandQueryDTO;
import com.example.baseboot.module.product.brand.dto.BrandUpdateDTO;
import com.example.baseboot.module.product.brand.entity.Brand;
import com.example.baseboot.module.product.brand.vo.BrandVO;

import java.util.List;

/**
 * 商品品牌业务接口
 */
public interface BrandService extends IService<Brand> {

    /**
     * 创建商品品牌
     */
    BrandVO createBrand(BrandCreateDTO createDTO);

    /**
     * 修改商品品牌
     */
    BrandVO updateBrand(Long id, BrandUpdateDTO updateDTO);

    /**
     * 删除商品品牌
     */
    boolean deleteBrand(Long id);

    /**
     * 根据ID获取品牌详情
     */
    BrandVO getBrandById(Long id);

    /**
     * 分页多条件查询品牌列表
     */
    CommonPage<BrandVO> pageBrands(BrandQueryDTO queryDTO);

    /**
     * 获取所有启用的品牌列表 (下拉选择)
     */
    List<BrandVO> listAllBrands();

    /**
     * 修改品牌启用状态
     */
    boolean updateStatus(Long id, Integer status);
}
