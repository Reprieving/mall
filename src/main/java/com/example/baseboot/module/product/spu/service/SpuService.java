package com.example.baseboot.module.product.spu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.product.spu.dto.SpuCreateDTO;
import com.example.baseboot.module.product.spu.dto.SpuQueryDTO;
import com.example.baseboot.module.product.spu.dto.SpuUpdateDTO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.vo.SpuDetailVO;
import com.example.baseboot.module.product.spu.vo.SpuVO;

import java.util.List;

/**
 * 商品 SPU 业务接口
 */
public interface SpuService extends IService<Spu> {

    /**
     * 一体化创建 SPU (包含规格定义与 SKU 列表，自动计算聚合价格/库存)
     */
    SpuDetailVO createSpu(SpuCreateDTO createDTO);

    /**
     * 更新 SPU (包含基本信息、规格定义与 SKU 维护)
     */
    SpuDetailVO updateSpu(Long id, SpuUpdateDTO updateDTO);

    /**
     * 删除 SPU (级联删除规格项与所有 SKU)
     */
    boolean deleteSpu(Long id);

    /**
     * 查询 SPU 完整详情 (基础信息 + 分类 + 品牌 + 规格项 + SKU 列表)
     */
    SpuDetailVO getSpuDetail(Long id);

    /**
     * 分页多条件查询 SPU 列表
     */
    CommonPage<SpuVO> pageSpu(SpuQueryDTO queryDTO);

    /**
     * 单个商品上下架状态变更
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 批量商品上下架状态变更
     */
    boolean batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 根据旗下有效 SKU 重新计算并同步 SPU 的最低售价、最高售价与总库存
     */
    void recalculateSpuStockAndPrice(Long spuId);
}
