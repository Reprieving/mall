package com.example.baseboot.module.product.spec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.product.spec.dto.SpecKeyCreateDTO;
import com.example.baseboot.module.product.spec.dto.SpecKeyQueryDTO;
import com.example.baseboot.module.product.spec.dto.SpecKeyUpdateDTO;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.vo.SpecKeyVO;

import java.util.List;

/**
 * 商品规格项业务接口
 */
public interface SpecKeyService extends IService<SpecKey> {

    /**
     * 创建规格项 (支持附带初始规格值)
     */
    SpecKeyVO createSpecKey(SpecKeyCreateDTO createDTO);

    /**
     * 修改规格项
     */
    SpecKeyVO updateSpecKey(Long id, SpecKeyUpdateDTO updateDTO);

    /**
     * 删除规格项 (引用校验)
     */
    boolean deleteSpecKey(Long id);

    /**
     * 获取规格项详情 (含规格值列表)
     */
    SpecKeyVO getSpecKeyById(Long id);

    /**
     * 分页查询规格项列表
     */
    CommonPage<SpecKeyVO> pageSpecKeys(SpecKeyQueryDTO queryDTO);

    /**
     * 根据分类 ID 查询该分类下所有可用的规格模板及可选值
     */
    List<SpecKeyVO> listSpecsByCategory(Long categoryId);

    /**
     * 修改规格项启用状态
     */
    boolean updateStatus(Long id, Integer status);
}
