package com.example.baseboot.module.product.spec.util;

import com.example.baseboot.common.api.ResultCode;
import com.example.baseboot.common.exception.BusinessException;
import com.example.baseboot.module.product.sku.entity.Sku;
import com.example.baseboot.module.product.spec.dto.SkuSpecValueItemDTO;
import com.example.baseboot.module.product.spec.entity.SpecKey;
import com.example.baseboot.module.product.spec.entity.SpecValue;
import com.example.baseboot.module.product.spec.mapper.SpecKeyMapper;
import com.example.baseboot.module.product.spec.mapper.SpecValueMapper;
import com.example.baseboot.module.product.spec.vo.SkuSpecValueVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * SKU 规格签名与唯一性校验工具类
 */
public class SkuSpecUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Getter
    public static class SpecSignature {
        private final String signature;
        private final String readableDesc;

        public SpecSignature(String signature, String readableDesc) {
            this.signature = signature != null ? signature : "";
            this.readableDesc = readableDesc != null ? readableDesc : "";
        }

        public boolean isEmpty() {
            return !StringUtils.hasText(signature);
        }
    }

    /**
     * 提取规格签名 (支持 DTO、VO、JSON specData 或文本 specData)
     */
    public static SpecSignature extractSignature(
            List<?> items,
            String specData,
            SpecKeyMapper specKeyMapper,
            SpecValueMapper specValueMapper
    ) {
        Map<String, String> specMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        if (!CollectionUtils.isEmpty(items)) {
            for (Object obj : items) {
                if (obj == null) continue;
                String k = null;
                String v = null;
                Long keyId = null;
                Long valId = null;

                if (obj instanceof SkuSpecValueItemDTO) {
                    SkuSpecValueItemDTO dto = (SkuSpecValueItemDTO) obj;
                    k = dto.getSpecKeyName();
                    v = dto.getSpecValue();
                    keyId = dto.getSpecKeyId();
                    valId = dto.getSpecValueId();
                } else if (obj instanceof SkuSpecValueVO) {
                    SkuSpecValueVO vo = (SkuSpecValueVO) obj;
                    k = vo.getSpecKeyName();
                    v = vo.getSpecValue();
                    keyId = vo.getSpecKeyId();
                    valId = vo.getSpecValueId();
                }

                if (!StringUtils.hasText(k) && keyId != null && specKeyMapper != null) {
                    SpecKey specKey = specKeyMapper.selectById(keyId);
                    if (specKey != null) {
                        k = specKey.getName();
                    }
                }
                if (!StringUtils.hasText(k) && keyId != null) {
                    k = "KEY_" + keyId;
                }

                if (!StringUtils.hasText(v) && valId != null && specValueMapper != null) {
                    SpecValue specVal = specValueMapper.selectById(valId);
                    if (specVal != null) {
                        v = specVal.getValue();
                    }
                }
                if (!StringUtils.hasText(v) && valId != null) {
                    v = "VAL_" + valId;
                }

                if (StringUtils.hasText(k) && StringUtils.hasText(v)) {
                    specMap.put(k.trim(), v.trim());
                }
            }
        }

        // 如果 items 为空，尝试从 specData 解析
        if (specMap.isEmpty() && StringUtils.hasText(specData)) {
            parseSpecData(specData, specKeyMapper, specValueMapper, specMap);
        }

        if (specMap.isEmpty()) {
            return new SpecSignature("", "");
        }

        StringBuilder sig = new StringBuilder();
        StringBuilder desc = new StringBuilder();
        for (Map.Entry<String, String> entry : specMap.entrySet()) {
            if (sig.length() > 0) {
                sig.append(";");
                desc.append(", ");
            }
            sig.append(entry.getKey().toLowerCase()).append(":").append(entry.getValue().toLowerCase());
            desc.append(entry.getKey()).append(": ").append(entry.getValue());
        }

        return new SpecSignature(sig.toString(), desc.toString());
    }

    private static void parseSpecData(
            String specData,
            SpecKeyMapper specKeyMapper,
            SpecValueMapper specValueMapper,
            Map<String, String> specMap
    ) {
        String trimmed = specData.trim();
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(trimmed);
                if (root.isArray()) {
                    for (JsonNode item : root) {
                        String k = null;
                        if (item.hasNonNull("specKeyName")) {
                            k = item.get("specKeyName").asText();
                        } else if (item.hasNonNull("specKeyId") && specKeyMapper != null) {
                            SpecKey sk = specKeyMapper.selectById(item.get("specKeyId").asLong());
                            if (sk != null) k = sk.getName();
                        }
                        if (!StringUtils.hasText(k) && item.hasNonNull("specKeyId")) {
                            k = "KEY_" + item.get("specKeyId").asLong();
                        }

                        String v = null;
                        if (item.hasNonNull("specValue")) {
                            v = item.get("specValue").asText();
                        } else if (item.hasNonNull("specValueId") && specValueMapper != null) {
                            SpecValue sv = specValueMapper.selectById(item.get("specValueId").asLong());
                            if (sv != null) v = sv.getValue();
                        }
                        if (!StringUtils.hasText(v) && item.hasNonNull("specValueId")) {
                            v = "VAL_" + item.get("specValueId").asLong();
                        }

                        if (StringUtils.hasText(k) && StringUtils.hasText(v)) {
                            specMap.put(k.trim(), v.trim());
                        }
                    }
                } else if (root.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        if (StringUtils.hasText(entry.getKey()) && entry.getValue() != null && !entry.getValue().isNull()) {
                            specMap.put(entry.getKey().trim(), entry.getValue().asText().trim());
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 兼容非 JSON 的文本格式："机身颜色:经典黑;存储容量:M" 或 "机身颜色:经典黑,存储容量:M"
        if (specMap.isEmpty()) {
            String[] pairs = trimmed.split("[;,\\n]+");
            for (String pair : pairs) {
                String[] kv = pair.split("[:=：]", 2);
                if (kv.length == 2 && StringUtils.hasText(kv[0]) && StringUtils.hasText(kv[1])) {
                    specMap.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
    }

    /**
     * 校验待保存/更新的 SKU 规格是否与同 SPU 下已有的其他 SKU 冲突
     */
    public static void checkDuplicateSpecWithExisting(
            SpecSignature targetSig,
            List<Sku> existingOtherSkus,
            Map<Long, List<SkuSpecValueVO>> existingSkuSpecMap,
            SpecKeyMapper specKeyMapper,
            SpecValueMapper specValueMapper
    ) {
        if (targetSig == null || targetSig.isEmpty() || CollectionUtils.isEmpty(existingOtherSkus)) {
            return;
        }

        for (Sku other : existingOtherSkus) {
            List<SkuSpecValueVO> voList = existingSkuSpecMap != null ? existingSkuSpecMap.get(other.getId()) : null;
            SpecSignature otherSig = extractSignature(voList, other.getSpecData(), specKeyMapper, specValueMapper);
            if (!otherSig.isEmpty() && targetSig.getSignature().equals(otherSig.getSignature())) {
                String codeOrName = StringUtils.hasText(other.getSkuCode()) ? other.getSkuCode() : other.getName();
                throw new BusinessException(ResultCode.VALIDATE_FAILED,
                        "当前商品已存在相同的SKU规格: " + targetSig.getReadableDesc()
                                + "，不允许提交重复规格 (与已有SKU " + codeOrName + " 冲突)");
            }
        }
    }
}
