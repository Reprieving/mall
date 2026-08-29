package com.example.baseboot.module.user.address.vo;

import com.example.baseboot.module.user.address.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户收货地址视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String fullAddress;
    private String postalCode;
    private Integer isDefault;
    private String tag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AddressVO fromEntity(UserAddress entity) {
        if (entity == null) {
            return null;
        }
        String full = String.format("%s%s%s%s",
                entity.getProvince() != null ? entity.getProvince() : "",
                entity.getCity() != null ? entity.getCity() : "",
                entity.getDistrict() != null ? entity.getDistrict() : "",
                entity.getDetailAddress() != null ? entity.getDetailAddress() : "");

        return AddressVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .province(entity.getProvince())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .detailAddress(entity.getDetailAddress())
                .fullAddress(full)
                .postalCode(entity.getPostalCode())
                .isDefault(entity.getIsDefault())
                .tag(entity.getTag())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
