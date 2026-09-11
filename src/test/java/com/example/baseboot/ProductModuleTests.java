package com.example.baseboot;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.vo.CategoryTreeVO;
import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.spec.dto.SkuSpecValueItemDTO;
import com.example.baseboot.module.product.spec.entity.SkuSpecValue;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;
import com.example.baseboot.module.product.spec.vo.SpecKeyVO;
import com.example.baseboot.module.product.spec.vo.SpecValueVO;
import com.example.baseboot.module.product.spu.entity.Spu;
import com.example.baseboot.module.product.spu.vo.SpuDetailVO;
import com.example.baseboot.module.product.spu.vo.SpuSpecVO;
import com.example.baseboot.module.product.spu.vo.SpuVO;
import com.example.baseboot.module.shop.entity.Shop;
import com.example.baseboot.module.shop.vo.ShopVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

class ProductModuleTests {

    @Test
    void testCategoryTreeAssembly() {
        List<Category> allCategories = Arrays.asList(
                Category.builder().id(1L).parentId(0L).name("数码电子").sort(1).level(1).build(),
                Category.builder().id(2L).parentId(1L).name("手机通讯").sort(1).level(2).build(),
                Category.builder().id(3L).parentId(2L).name("智能手机").sort(1).level(3).build(),
                Category.builder().id(4L).parentId(1L).name("电脑办公").sort(2).level(2).build(),
                Category.builder().id(5L).parentId(0L).name("服装服饰").sort(2).level(1).build()
        );

        Map<Long, CategoryTreeVO> nodeMap = new HashMap<>(allCategories.size());
        List<CategoryTreeVO> rootNodes = new ArrayList<>();

        for (Category cat : allCategories) {
            nodeMap.put(cat.getId(), CategoryTreeVO.fromEntity(cat));
        }

        for (Category cat : allCategories) {
            CategoryTreeVO currentNode = nodeMap.get(cat.getId());
            Long parentId = cat.getParentId();
            if (parentId == null || parentId == 0 || !nodeMap.containsKey(parentId)) {
                rootNodes.add(currentNode);
            } else {
                CategoryTreeVO parentNode = nodeMap.get(parentId);
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList<>());
                }
                parentNode.getChildren().add(currentNode);
            }
        }

        Assertions.assertEquals(2, rootNodes.size());
        CategoryTreeVO digitalCategory = rootNodes.get(0);
        Assertions.assertEquals("数码电子", digitalCategory.getName());
        Assertions.assertEquals(2, digitalCategory.getChildren().size());

        CategoryTreeVO phoneCategory = digitalCategory.getChildren().get(0);
        Assertions.assertEquals("手机通讯", phoneCategory.getName());
        Assertions.assertEquals(1, phoneCategory.getChildren().size());
        Assertions.assertEquals("智能手机", phoneCategory.getChildren().get(0).getName());
    }

    @Test
    void testMultiSpecPriceAndStockCalculation() {
        List<SkuItemDTO> skus = Arrays.asList(
                SkuItemDTO.builder().skuCode("SKU001").name("iPhone 15 Pro 黑色 128G").price(new BigDecimal("7999.00")).stock(50).build(),
                SkuItemDTO.builder().skuCode("SKU002").name("iPhone 15 Pro 黑色 256G").price(new BigDecimal("8999.00")).stock(60).build(),
                SkuItemDTO.builder().skuCode("SKU003").name("iPhone 15 Pro 白色 128G").price(new BigDecimal("7999.00")).stock(40).build(),
                SkuItemDTO.builder().skuCode("SKU004").name("iPhone 15 Pro 白色 256G").price(new BigDecimal("8999.00")).stock(30).build()
        );

        BigDecimal minPrice = skus.stream()
                .map(SkuItemDTO::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxPrice = skus.stream()
                .map(SkuItemDTO::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        int totalStock = skus.stream()
                .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                .sum();

        Assertions.assertEquals(new BigDecimal("7999.00"), minPrice);
        Assertions.assertEquals(new BigDecimal("8999.00"), maxPrice);
        Assertions.assertEquals(180, totalStock);
    }

    @Test
    void testPatternBSpecKeyAndValueVO() {
        SpecKey key = SpecKey.builder()
                .id(1L)
                .categoryId(3L)
                .name("机身颜色")
                .sort(1)
                .status(1)
                .createTime(LocalDateTime.now())
                .build();

        SpecValue val1 = SpecValue.builder().id(101L).specKeyId(1L).value("暗夜黑").sort(1).status(1).build();
        SpecValue val2 = SpecValue.builder().id(102L).specKeyId(1L).value("原色钛金属").sort(2).status(1).build();

        SpecKeyVO keyVO = SpecKeyVO.fromEntity(key);
        keyVO.setValues(Arrays.asList(SpecValueVO.fromEntity(val1), SpecValueVO.fromEntity(val2)));

        Assertions.assertEquals("机身颜色", keyVO.getName());
        Assertions.assertEquals(2, keyVO.getValues().size());
        Assertions.assertEquals("暗夜黑", keyVO.getValues().get(0).getValue());
        Assertions.assertEquals("原色钛金属", keyVO.getValues().get(1).getValue());
    }

    @Test
    void testSkuSpecValueRelation() {
        SkuSpecValue relation1 = SkuSpecValue.builder()
                .id(1L)
                .skuId(501L)
                .spuId(101L)
                .specKeyId(1L)
                .specKeyName("机身颜色")
                .specValueId(101L)
                .specValue("暗夜黑")
                .build();

        SkuSpecValue relation2 = SkuSpecValue.builder()
                .id(2L)
                .skuId(501L)
                .spuId(101L)
                .specKeyId(2L)
                .specKeyName("存储容量")
                .specValueId(201L)
                .specValue("256GB")
                .build();

        SkuSpecValueVO vo1 = SkuSpecValueVO.fromEntity(relation1);
        SkuSpecValueVO vo2 = SkuSpecValueVO.fromEntity(relation2);

        Assertions.assertEquals(1L, vo1.getSpecKeyId());
        Assertions.assertEquals("机身颜色", vo1.getSpecKeyName());
        Assertions.assertEquals(101L, vo1.getSpecValueId());
        Assertions.assertEquals("暗夜黑", vo1.getSpecValue());

        Assertions.assertEquals(2L, vo2.getSpecKeyId());
        Assertions.assertEquals("存储容量", vo2.getSpecKeyName());
        Assertions.assertEquals(201L, vo2.getSpecValueId());
        Assertions.assertEquals("256GB", vo2.getSpecValue());
    }

    @Test
    void testSpuShopAssociation() {
        Spu spu = Spu.builder()
                .id(1L)
                .shopId(1L)
                .name("Apple iPhone 15 Pro")
                .spuCode("SPU-IPHONE15P")
                .minPrice(new BigDecimal("7999.00"))
                .maxPrice(new BigDecimal("8999.00"))
                .totalStock(200)
                .status(1)
                .build();

        Shop shop = Shop.builder()
                .id(1L)
                .name("极光数码官方旗舰店")
                .type(3)
                .status(1)
                .build();

        SpuVO spuVO = SpuVO.fromEntity(spu);
        spuVO.setShopName(shop.getName());

        SpuDetailVO detailVO = SpuDetailVO.builder()
                .spuInfo(spuVO)
                .shop(ShopVO.fromEntity(shop))
                .build();

        Assertions.assertEquals(1L, detailVO.getSpuInfo().getShopId());
        Assertions.assertEquals("极光数码官方旗舰店", detailVO.getSpuInfo().getShopName());
        Assertions.assertNotNull(detailVO.getShop());
        Assertions.assertEquals("极光数码官方旗舰店", detailVO.getShop().getName());
    }

    @Test
    void testCommonPageRestPage() {
        Page<String> page = new Page<>(1, 10);
        page.setTotal(25);
        page.setRecords(Arrays.asList("Item1", "Item2", "Item3"));

        CommonPage<String> commonPage = CommonPage.restPage(page);
        Assertions.assertEquals(1L, commonPage.getPageNum());
        Assertions.assertEquals(10L, commonPage.getPageSize());
        Assertions.assertEquals(25L, commonPage.getTotal());
        Assertions.assertEquals(3L, commonPage.getTotalPages());
        Assertions.assertEquals(3, commonPage.getList().size());
    }

    @Test
    void testMapBySkuIdsNoDuplication() {
        List<SkuSpecValue> records = Arrays.asList(
                SkuSpecValue.builder().id(1L).skuId(11L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("经典黑").build(),
                SkuSpecValue.builder().id(2L).skuId(11L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("M").build(),
                SkuSpecValue.builder().id(3L).skuId(12L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("经典黑").build(),
                SkuSpecValue.builder().id(4L).skuId(12L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(102L).specValue("L").build()
        );

        Map<Long, List<SkuSpecValueVO>> skuSpecMap = records.stream().collect(java.util.stream.Collectors.groupingBy(
                SkuSpecValue::getSkuId,
                java.util.stream.Collectors.mapping(SkuSpecValueVO::fromEntity, java.util.stream.Collectors.toList())
        ));

        Assertions.assertEquals(2, skuSpecMap.size());
        List<SkuSpecValueVO> sku11Specs = skuSpecMap.get(11L);
        Assertions.assertEquals(2, sku11Specs.size());
        Assertions.assertEquals("颜色", sku11Specs.get(0).getSpecKeyName());
        Assertions.assertEquals("尺码", sku11Specs.get(1).getSpecKeyName());
    }

    @Test
    void testSpuSpecMatrixDeduplicationAndUnion() {
        // 模拟 SPU3 场景：已有规格选用关系只有 经典黑(1) 与 L(101)
        // 但 6 个 SKU 涵盖了 经典黑、象牙白 以及 M、L、XL，且部分带有重复/虚拟 specValueId
        List<SkuSpecValue> skuSpecValues = Arrays.asList(
                // SKU 1: 经典黑 + M
                SkuSpecValue.builder().skuId(1L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("经典黑").build(),
                SkuSpecValue.builder().skuId(1L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("M").build(),
                // SKU 2: 经典黑 + L
                SkuSpecValue.builder().skuId(2L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("经典黑").build(),
                SkuSpecValue.builder().skuId(2L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("L").build(),
                // SKU 3: 经典黑 + XL
                SkuSpecValue.builder().skuId(3L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("经典黑").build(),
                SkuSpecValue.builder().skuId(3L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("XL").build(),
                // SKU 4: 象牙白 + M
                SkuSpecValue.builder().skuId(4L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("象牙白").build(),
                SkuSpecValue.builder().skuId(4L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("M").build(),
                // SKU 5: 象牙白 + L
                SkuSpecValue.builder().skuId(5L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("象牙白").build(),
                SkuSpecValue.builder().skuId(5L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("L").build(),
                // SKU 6: 象牙白 + XL
                SkuSpecValue.builder().skuId(6L).spuId(3L).specKeyId(1L).specKeyName("颜色").specValueId(1L).specValue("象牙白").build(),
                SkuSpecValue.builder().skuId(6L).spuId(3L).specKeyId(2L).specKeyName("尺码").specValueId(101L).specValue("XL").build()
        );

        // 基于文本维度的聚合算法验证 (杜绝旧实现按 specValueId putIfAbsent 导致的值被吞)
        Map<Long, String> keyNameMap = new LinkedHashMap<>();
        Map<Long, Set<String>> keyValueSetMap = new LinkedHashMap<>();

        for (SkuSpecValue ssv : skuSpecValues) {
            if (ssv.getSpecKeyId() != null && ssv.getSpecValue() != null) {
                keyNameMap.putIfAbsent(ssv.getSpecKeyId(), ssv.getSpecKeyName());
                keyValueSetMap.computeIfAbsent(ssv.getSpecKeyId(), k -> new LinkedHashSet<>())
                        .add(ssv.getSpecValue().trim());
            }
        }

        Assertions.assertEquals(2, keyNameMap.size());
        Assertions.assertEquals("颜色", keyNameMap.get(1L));
        Assertions.assertEquals("尺码", keyNameMap.get(2L));

        Set<String> colorValues = keyValueSetMap.get(1L);
        Assertions.assertEquals(2, colorValues.size(), "颜色应包含 经典黑 与 象牙白 2种取值");
        Assertions.assertTrue(colorValues.contains("经典黑"));
        Assertions.assertTrue(colorValues.contains("象牙白"));

        Set<String> sizeValues = keyValueSetMap.get(2L);
        Assertions.assertEquals(3, sizeValues.size(), "尺码应包含 M、L、XL 3种取值");
        Assertions.assertTrue(sizeValues.contains("M"));
        Assertions.assertTrue(sizeValues.contains("L"));
        Assertions.assertTrue(sizeValues.contains("XL"));
    }

    @Test
    void testOrderIndependentSkuSpecSignature() {
        // SKU A: 颜色 先，尺码 后
        List<SkuSpecValueItemDTO> specsA = Arrays.asList(
                SkuSpecValueItemDTO.builder().specKeyName("颜色").specValue("经典黑").build(),
                SkuSpecValueItemDTO.builder().specKeyName("尺码").specValue("L").build()
        );

        // SKU B: 尺码 先，颜色 后
        List<SkuSpecValueItemDTO> specsB = Arrays.asList(
                SkuSpecValueItemDTO.builder().specKeyName("尺码").specValue("L").build(),
                SkuSpecValueItemDTO.builder().specKeyName("颜色").specValue("经典黑").build()
        );

        // 提取排序后签名
        Map<String, String> mapA = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (SkuSpecValueItemDTO item : specsA) {
            mapA.put(item.getSpecKeyName().trim(), item.getSpecValue().trim());
        }

        Map<String, String> mapB = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (SkuSpecValueItemDTO item : specsB) {
            mapB.put(item.getSpecKeyName().trim(), item.getSpecValue().trim());
        }

        StringBuilder sigA = new StringBuilder();
        mapA.forEach((k, v) -> sigA.append(k.toLowerCase()).append(":").append(v.toLowerCase()).append(";"));

        StringBuilder sigB = new StringBuilder();
        mapB.forEach((k, v) -> sigB.append(k.toLowerCase()).append(":").append(v.toLowerCase()).append(";"));

        Assertions.assertEquals(sigA.toString(), sigB.toString(), "不同顺序的规格维度应生成完全一致的签名");
        Assertions.assertEquals("尺码:l;颜色:经典黑;", sigA.toString());
    }

    @Test
    void testDuplicateSkuSpecificationDetectionInUpdate() {
        // 模拟 SPU 提交包含重复规格组合的 SKU 列表
        List<SkuItemDTO> incomingSkus = Arrays.asList(
                SkuItemDTO.builder().skuCode("SKU001").specValues(Arrays.asList(
                        SkuSpecValueItemDTO.builder().specKeyName("颜色").specValue("经典黑").build(),
                        SkuSpecValueItemDTO.builder().specKeyName("尺码").specValue("L").build()
                )).build(),
                SkuItemDTO.builder().skuCode("SKU002").specValues(Arrays.asList(
                        SkuSpecValueItemDTO.builder().specKeyName("尺码").specValue("L").build(),
                        SkuSpecValueItemDTO.builder().specKeyName("颜色").specValue("经典黑").build()
                )).build()
        );

        Map<String, String> seenSignatures = new HashMap<>();
        boolean duplicateDetected = false;
        String duplicatedDesc = "";

        for (SkuItemDTO sku : incomingSkus) {
            Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (SkuSpecValueItemDTO sv : sku.getSpecValues()) {
                map.put(sv.getSpecKeyName().trim(), sv.getSpecValue().trim());
            }
            StringBuilder sig = new StringBuilder();
            StringBuilder desc = new StringBuilder();
            map.forEach((k, v) -> {
                sig.append(k.toLowerCase()).append(":").append(v.toLowerCase()).append(";");
                desc.append(k).append(": ").append(v).append(", ");
            });
            if (seenSignatures.containsKey(sig.toString())) {
                duplicateDetected = true;
                duplicatedDesc = desc.toString();
                break;
            }
            seenSignatures.put(sig.toString(), sku.getSkuCode());
        }

        Assertions.assertTrue(duplicateDetected, "应当检测到重复的规格组合并拦截");
        Assertions.assertTrue(duplicatedDesc.contains("颜色: 经典黑") && duplicatedDesc.contains("尺码: L"));
    }

    @Test
    void testSkuSpecUtilsExtractSignatureAndDuplicateDetection() {
        // 1. 测试从纯文本 specData 中提取签名
        String textSpecData = "机身颜色:经典黑;存储容量:M";
        com.example.baseboot.module.product.spec.util.SkuSpecUtils.SpecSignature sig1 =
                com.example.baseboot.module.product.spec.util.SkuSpecUtils.extractSignature(null, textSpecData, null, null);
        Assertions.assertFalse(sig1.isEmpty());
        Assertions.assertEquals("存储容量:m;机身颜色:经典黑", sig1.getSignature());

        // 2. 测试从 SkuSpecValueItemDTO 列表中提取签名 (无视字段顺序与大小写)
        List<SkuSpecValueItemDTO> dtoList = Arrays.asList(
                SkuSpecValueItemDTO.builder().specKeyName("存储容量").specValue("m").build(),
                SkuSpecValueItemDTO.builder().specKeyName("机身颜色").specValue("经典黑").build()
        );
        com.example.baseboot.module.product.spec.util.SkuSpecUtils.SpecSignature sig2 =
                com.example.baseboot.module.product.spec.util.SkuSpecUtils.extractSignature(dtoList, null, null, null);
        Assertions.assertEquals(sig1.getSignature(), sig2.getSignature(), "相同规格维度不论输入格式或大小写，签名必须一致");

        // 3. 测试与同 SPU 已有 SKU 冲突校验
        Sku existingSku = Sku.builder().id(11L).spuId(3L).skuCode("SPU394629-01").name("经典黑 / M").build();
        List<SkuSpecValueVO> existingVOs = Arrays.asList(
                SkuSpecValueVO.builder().specKeyName("机身颜色").specValue("经典黑").build(),
                SkuSpecValueVO.builder().specKeyName("存储容量").specValue("M").build()
        );
        Map<Long, List<SkuSpecValueVO>> specMap = Collections.singletonMap(11L, existingVOs);

        // 尝试更新/新增 SKU 为相同规格组合 -> 必须抛出 BusinessException
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> {
            com.example.baseboot.module.product.spec.util.SkuSpecUtils.checkDuplicateSpecWithExisting(
                    sig2,
                    Collections.singletonList(existingSku),
                    specMap,
                    null,
                    null
            );
        });
        Assertions.assertEquals(ResultCode.VALIDATE_FAILED.getCode(), ex.getCode());
        Assertions.assertTrue(ex.getMessage().contains("当前商品已存在相同的SKU规格"));
        Assertions.assertTrue(ex.getMessage().contains("SPU394629-01"));

        // 4. 不同规格的 SKU -> 不冲突
        List<SkuSpecValueItemDTO> differentDtoList = Arrays.asList(
                SkuSpecValueItemDTO.builder().specKeyName("机身颜色").specValue("经典黑").build(),
                SkuSpecValueItemDTO.builder().specKeyName("存储容量").specValue("256GB").build()
        );
        com.example.baseboot.module.product.spec.util.SkuSpecUtils.SpecSignature diffSig =
                com.example.baseboot.module.product.spec.util.SkuSpecUtils.extractSignature(differentDtoList, null, null, null);
        Assertions.assertDoesNotThrow(() -> {
            com.example.baseboot.module.product.spec.util.SkuSpecUtils.checkDuplicateSpecWithExisting(
                    diffSig,
                    Collections.singletonList(existingSku),
                    specMap,
                    null,
                    null
            );
        });
    }
}
