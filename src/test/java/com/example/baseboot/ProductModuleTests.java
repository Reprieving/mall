package com.example.baseboot;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.baseboot.common.api.CommonPage;
import com.example.baseboot.module.product.category.entity.Category;
import com.example.baseboot.module.product.category.vo.CategoryTreeVO;
import com.example.baseboot.module.product.sku.dto.SkuItemDTO;
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
}
