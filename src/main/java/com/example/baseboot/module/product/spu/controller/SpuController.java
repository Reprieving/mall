package com.example.baseboot.module.product.spu.controller;

import com.example.baseboot.common.annotation.LoginRequired;
import com.example.baseboot.common.annotation.PassToken;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.module.product.spu.dto.BatchStatusDTO;
import com.example.baseboot.module.product.spu.dto.SpuCreateDTO;
import com.example.baseboot.module.product.spu.dto.SpuQueryDTO;
import com.example.baseboot.module.product.spu.dto.SpuUpdateDTO;
import com.example.baseboot.module.product.spu.service.SpuService;
import com.example.baseboot.module.product.spu.vo.SpuDetailVO;
import com.example.baseboot.module.product.spu.vo.SpuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品 SPU 控制器
 */
@Tag(name = "11. 商品SPU管控 (SpuController)", description = "商品 SPU 录入、矩阵生成、全量详情、多维检索与上下架控制")
@RestController
@RequestMapping("/api/spu")
@RequiredArgsConstructor
public class SpuController {

    private final SpuService spuService;



    /**
     * 分页查询 SPU 商品列表 (支持多条件组合查询)
     */
    @Operation(summary = "分页查询 SPU 列表", description = "多条件组合检索 SPU 列表，支持按关键字、类目、品牌、状态及价格范围筛选")
    @GetMapping("/page")
    @PassToken
    public CommonResult<CommonPage<SpuVO>> pageSpu(SpuQueryDTO queryDTO) {
        CommonPage<SpuVO> page = spuService.pageSpu(queryDTO);
        return CommonResult.success(page);
    }


}
