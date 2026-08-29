package com.example.baseboot.module.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺实体类 (对应表 sms_shop)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sms_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店主用户ID
     */
    private Long userId;

    /**
     * 关联认证记录ID (sys_user_certification.id)
     */
    private Long certId;

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 店铺 Logo 图标
     */
    private String logo;

    /**
     * 店铺横幅大图 / 门头招牌
     */
    private String banner;

    /**
     * 店铺简介
     */
    private String intro;

    /**
     * 店铺公告
     */
    private String notice;

    /**
     * 客服/联系电话
     */
    private String phone;

    /**
     * 店铺类型: 1-个人店, 2-个体工商户店, 3-企业旗舰店, 4-企业专营店
     */
    private Integer type;

    /**
     * 店铺状态: 0-待审核, 1-正常营业, 2-暂停营业(打烊), 3-审核驳回, 4-违规封禁
     */
    private Integer status;

    /**
     * 审核驳回原因
     */
    private String rejectReason;

    /**
     * 店铺评分 (1.00-5.00)
     */
    private BigDecimal score;

    /**
     * 申请时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
