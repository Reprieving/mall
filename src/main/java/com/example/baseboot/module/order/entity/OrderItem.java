package com.example.baseboot.module.order.entity;

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
 * 订单明细项与商品快照实体类 (对应表 oms_order_item)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oms_order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 商品 SPU ID
     */
    private Long spuId;

    /**
     * 商品名称 (下单快照)
     */
    private String spuName;

    /**
     * 商品主图 (下单快照)
     */
    private String spuPic;

    /**
     * 商品 SKU ID
     */
    private Long skuId;

    /**
     * SKU 编码 (下单快照)
     */
    private String skuCode;

    /**
     * SKU 名称 (下单快照)
     */
    private String skuName;

    /**
     * SKU 规格图片 (下单快照)
     */
    private String skuPic;

    /**
     * 购买单价 (下单快照)
     */
    private BigDecimal skuPrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 商品小计金额 (skuPrice * quantity)
     */
    private BigDecimal subtotalAmount;

    /**
     * 多规格属性快照 JSON
     */
    private String specData;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
