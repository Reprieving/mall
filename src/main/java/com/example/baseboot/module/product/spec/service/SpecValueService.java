package com.example.baseboot.module.product.spec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.module.product.spec.dto.SpecValueBatchDTO;
import com.example.baseboot.module.product.spec.dto.SpecValueCreateDTO;
import com.example.baseboot.module.product.spec.dto.SpecValueUpdateDTO;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.vo.SpecValueVO;

import java.util.List;

/**
 * 商品规格值业务接口
 */
public interface SpecValueService extends IService<SpecValue> {

    /**
     * 新增单个规格值
     */
    SpecValueVO createSpecValue(SpecValueCreateDTO createDTO);

    /**
     * 批量新增规格值
     */
    List<SpecValueVO> batchCreateSpecValues(SpecValueBatchDTO batchDTO);

    /**
     * 修改规格值
     */
    SpecValueVO updateSpecValue(Long id, SpecValueUpdateDTO updateDTO);

    /**
     * 删除规格值 (引用校验)
     */
    boolean deleteSpecValue(Long id);

    /**
     * 获取规格值详情
     */
    SpecValueVO getSpecValueById(Long id);

    /**
     * 查询指定规格项下的所有规格值列表
     */
    List<SpecValueVO> listValuesBySpecKeyId(Long specKeyId);

    /**
     * 修改规格值启用状态
     */
    boolean updateStatus(Long id, Integer status);
}
