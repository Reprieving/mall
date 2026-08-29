package com.example.baseboot.module.shop.vo;

import com.example.baseboot.module.shop.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long certId;
    private String name;
    private String logo;
    private String banner;
    private String intro;
    private String notice;
    private String phone;
    private Integer type;
    private String typeName;
    private Integer status;
    private String statusName;
    private String rejectReason;
    private BigDecimal score;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ShopVO fromEntity(Shop entity) {
        if (entity == null) {
            return null;
        }

        String typeName = switch (entity.getType() != null ? entity.getType() : 1) {
            case 1 -> "个人店";
            case 2 -> "个体工商户店";
            case 3 -> "企业旗舰店";
            case 4 -> "企业专营店";
            default -> "普通店铺";
        };

        String statusName = switch (entity.getStatus() != null ? entity.getStatus() : 0) {
            case 0 -> "待审核";
            case 1 -> "正常营业";
            case 2 -> "暂停营业(打烊)";
            case 3 -> "审核驳回";
            case 4 -> "违规封禁";
            default -> "未知状态";
        };

        return ShopVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .certId(entity.getCertId())
                .name(entity.getName())
                .logo(entity.getLogo())
                .banner(entity.getBanner())
                .intro(entity.getIntro())
                .notice(entity.getNotice())
                .phone(entity.getPhone())
                .type(entity.getType())
                .typeName(typeName)
                .status(entity.getStatus())
                .statusName(statusName)
                .rejectReason(entity.getRejectReason())
                .score(entity.getScore())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
