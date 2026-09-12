package com.example.baseboot.module.order.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单退款表结构初始化器 (确保服务启动时自动检查并创建 oms_order_refund 表)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRefundTableInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS `oms_order_refund` (
                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    `refund_sn` VARCHAR(64) NOT NULL COMMENT '退款流水号 (全局唯一)',
                    `order_id` BIGINT NOT NULL COMMENT '所属订单ID',
                    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单流水号',
                    `shop_id` BIGINT NOT NULL COMMENT '所属店铺ID',
                    `user_id` BIGINT NOT NULL COMMENT '申请买家用户ID',
                    `refund_type` TINYINT NOT NULL DEFAULT 1 COMMENT '退款类型: 1-仅退款, 2-退货退款',
                    `refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '申请退款金额',
                    `reason` VARCHAR(255) NOT NULL COMMENT '退款原因',
                    `description` VARCHAR(500) DEFAULT NULL COMMENT '退款说明/补充描述',
                    `proof_pics` TEXT DEFAULT NULL COMMENT '凭证图片URL (逗号分隔或JSON)',
                    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '退款状态: 0-待审核, 1-审核通过(已退款), 2-审核驳回(已拒绝)',
                    `audit_time` DATETIME DEFAULT NULL COMMENT '审批时间',
                    `audit_user_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
                    `audit_user_name` VARCHAR(64) DEFAULT NULL COMMENT '审批人姓名/账号',
                    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批备注/驳回原因',
                    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (`id`),
                    UNIQUE KEY `uk_refund_sn` (`refund_sn`),
                    KEY `idx_order_id` (`order_id`),
                    KEY `idx_order_sn` (`order_sn`),
                    KEY `idx_shop_id` (`shop_id`),
                    KEY `idx_user_id` (`user_id`),
                    KEY `idx_status` (`status`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单退款申请表';
                """;

            jdbcTemplate.execute(createTableSql);
            log.info("数据库 oms_order_refund 表结构检查/初始化完成");
        } catch (Exception e) {
            log.warn("检查/初始化 oms_order_refund 表失败 (可能权限受限或表已由外部维护): {}", e.getMessage());
        }
    }
}
