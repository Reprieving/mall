-- =======================================================
-- Database: base_boot
-- Description: 用户信息管理系统数据库初始化脚本
-- =======================================================

CREATE DATABASE IF NOT EXISTS `base_boot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `base_boot`;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email` VARCHAR(100) NOT NULL COMMENT '电子邮箱 (唯一)',
    `password` VARCHAR(100) NOT NULL COMMENT '加盐哈希密码 (BCrypt)',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号码',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-保密, 1-男, 2-女',
    `bio` VARCHAR(255) DEFAULT NULL COMMENT '个性签名/个人简介',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 1-正常, 0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- Records of sys_user
-- 默认密码统一为: 123456 (BCrypt 哈希密文)
-- ----------------------------
INSERT INTO `sys_user` (`id`, `email`, `password`, `username`, `nickname`, `avatar`, `phone`, `gender`, `bio`, `status`)
VALUES 
(1, 'admin@example.com', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin', '系统管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '13800138000', 1, '热爱编程，致力于写出高质量代码', 1),
(2, 'test@example.com', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'testuser', '测试体验官', 'https://api.dicebear.com/7.x/avataaars/svg?seed=test', '13900139000', 2, '正在测试用户模块功能', 1);

-- ----------------------------
-- Table structure for sys_user_certification (用户多主体实名认证表)
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_certification`;
CREATE TABLE `sys_user_certification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `cert_type` TINYINT NOT NULL DEFAULT 1 COMMENT '认证类型: 1-个人实名认证, 2-个体工商户认证, 3-企业认证',
    `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名 / 法人姓名 / 经营者姓名',
    `id_card` VARCHAR(32) NOT NULL COMMENT '身份证号码',
    `id_card_front_pic` VARCHAR(500) NOT NULL COMMENT '身份证人像面照片URL',
    `id_card_back_pic` VARCHAR(500) NOT NULL COMMENT '身份证国徽面照片URL',
    `company_name` VARCHAR(128) DEFAULT NULL COMMENT '企业全称 / 个体户字号名称',
    `business_license_no` VARCHAR(64) DEFAULT NULL COMMENT '统一社会信用代码 / 营业执照号',
    `business_license_pic` VARCHAR(500) DEFAULT NULL COMMENT '营业执照电子扫描件/照片URL',
    `company_address` VARCHAR(255) DEFAULT NULL COMMENT '企业注册地址 / 经营场所',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-审核通过, 2-审核驳回',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核批注/驳回原因',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_cert_type` (`cert_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户多主体实名认证表';

-- 预置已通过认证的测试数据 (用户1 admin 已通过企业认证)
INSERT INTO `sys_user_certification` (`id`, `user_id`, `cert_type`, `real_name`, `id_card`, `id_card_front_pic`, `id_card_back_pic`, `company_name`, `business_license_no`, `business_license_pic`, `company_address`, `status`, `audit_time`)
VALUES
(1, 1, 3, '张伟', '110101199003072391', 'https://example.com/idcard/front.jpg', 'https://example.com/idcard/back.jpg', '北京极光科技有限公司', '91110108MA01ABCD12', 'https://example.com/cert/license.jpg', '北京市海淀区中关村南大街1号', 1, NOW());

-- ----------------------------
-- Table structure for ums_user_address (用户收货地址表)
-- ----------------------------
DROP TABLE IF EXISTS `ums_user_address`;
CREATE TABLE `ums_user_address` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(32) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(64) NOT NULL COMMENT '省份/直辖市',
    `city` VARCHAR(64) NOT NULL COMMENT '城市',
    `district` VARCHAR(64) NOT NULL COMMENT '区/县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址 (街道门牌号)',
    `postal_code` VARCHAR(16) DEFAULT NULL COMMENT '邮政编码',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认地址: 0-否, 1-是',
    `tag` VARCHAR(32) DEFAULT NULL COMMENT '地址标签 (如: 家, 公司, 学校)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- 预置示例收货地址数据
INSERT INTO `ums_user_address` (`id`, `user_id`, `name`, `phone`, `province`, `city`, `district`, `detail_address`, `postal_code`, `is_default`, `tag`)
VALUES 
(1, 1, '张伟', '13800138000', '北京市', '北京市', '海淀区', '中关村南大街1号院8号楼', '100086', 1, '公司'),
(2, 1, '张伟', '13800138000', '北京市', '北京市', '朝阳区', '望京SOHO T3 1801', '100102', 0, '家');

-- ----------------------------
-- Table structure for pms_category (商品分类表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_category`;
CREATE TABLE `pms_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID (0为顶级分类)',
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `level` INT NOT NULL DEFAULT 1 COMMENT '层级: 1-一级, 2-二级, 3-三级',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标URL',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 预置分类测试数据
INSERT INTO `pms_category` (`id`, `parent_id`, `name`, `level`, `icon`, `sort`, `status`) VALUES
(1, 0, '数码电子', 1, 'https://example.com/icons/digital.png', 1, 1),
(2, 1, '手机通讯', 2, 'https://example.com/icons/phone.png', 1, 1),
(3, 2, '智能手机', 3, 'https://example.com/icons/smartphone.png', 1, 1),
(4, 1, '电脑办公', 2, 'https://example.com/icons/computer.png', 2, 1),
(5, 4, '笔记本电脑', 3, 'https://example.com/icons/laptop.png', 1, 1),
(6, 0, '服装服饰', 1, 'https://example.com/icons/cloth.png', 2, 1);

-- ----------------------------
-- Table structure for pms_brand (商品品牌表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_brand`;
CREATE TABLE `pms_brand` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
    `name` VARCHAR(64) NOT NULL COMMENT '品牌名称',
    `logo` VARCHAR(255) DEFAULT NULL COMMENT '品牌Logo URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '品牌描述',
    `first_letter` CHAR(1) DEFAULT NULL COMMENT '检索首字母 (大写 A-Z)',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_first_letter` (`first_letter`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品品牌表';

-- 预置品牌测试数据
INSERT INTO `pms_brand` (`id`, `name`, `logo`, `description`, `first_letter`, `sort`, `status`) VALUES
(1, 'Apple / 苹果', 'https://example.com/brand/apple.png', 'Apple 设计制造的电子消费品', 'A', 1, 1),
(2, 'Huawei / 华为', 'https://example.com/brand/huawei.png', '构建万物互联的智能世界', 'H', 2, 1),
(3, 'Xiaomi / 小米', 'https://example.com/brand/xiaomi.png', '为发烧而生', 'X', 3, 1);

-- ----------------------------
-- Table structure for pms_spec_key (商品规格项表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_spec_key`;
CREATE TABLE `pms_spec_key` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规格项ID',
    `category_id` BIGINT NOT NULL DEFAULT 0 COMMENT '所属分类ID (0为通用规格)',
    `name` VARCHAR(64) NOT NULL COMMENT '规格项名称 (如: 颜色, 尺码, 内存)',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格项表';

-- 预置规格项测试数据
INSERT INTO `pms_spec_key` (`id`, `category_id`, `name`, `sort`, `status`) VALUES
(1, 3, '机身颜色', 1, 1),
(2, 3, '存储容量', 2, 1),
(3, 6, '服装尺码', 1, 1);

-- ----------------------------
-- Table structure for pms_spec_value (商品规格值表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_spec_value`;
CREATE TABLE `pms_spec_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规格值ID',
    `spec_key_id` BIGINT NOT NULL COMMENT '所属规格项ID',
    `value` VARCHAR(128) NOT NULL COMMENT '规格值 (如: 暗夜黑, 256GB, XXL)',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_spec_key_id` (`spec_key_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值表';

-- 预置规格值测试数据
INSERT INTO `pms_spec_value` (`id`, `spec_key_id`, `value`, `sort`, `status`) VALUES
(1, 1, '原色钛金属', 1, 1),
(2, 1, '暗夜黑', 2, 1),
(3, 2, '128GB', 1, 1),
(4, 2, '256GB', 2, 1),
(5, 3, 'M (中码)', 1, 1),
(6, 3, 'L (大码)', 2, 1),
(7, 3, 'XL (特大码)', 3, 1);

-- ----------------------------
-- Table structure for pms_spu (商品SPU表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_spu`;
CREATE TABLE `pms_spu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SPU ID',
    `shop_id` BIGINT NOT NULL DEFAULT 1 COMMENT '所属店铺ID',
    `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    `spu_code` VARCHAR(64) DEFAULT NULL COMMENT '商品编码/货号',
    `category_id` BIGINT NOT NULL COMMENT '所属分类ID',
    `brand_id` BIGINT DEFAULT NULL COMMENT '所属品牌ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '副标题/促销卖点',
    `description` LONGTEXT DEFAULT NULL COMMENT '商品图文详情',
    `main_pic` VARCHAR(500) DEFAULT NULL COMMENT '商品主图',
    `slider_pics` TEXT DEFAULT NULL COMMENT '轮播相册JSON/列表',
    `spec_type` TINYINT NOT NULL DEFAULT 1 COMMENT '规格类型: 0-单规格, 1-多规格',
    `min_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低售价/展示价',
    `max_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '最高售价',
    `total_stock` INT NOT NULL DEFAULT 0 COMMENT '商品总库存',
    `unit` VARCHAR(20) DEFAULT '件' COMMENT '计量单位',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '上架状态: 0-下架, 1-上架',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_code` (`spu_code`),
    KEY `idx_shop_id` (`shop_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_brand_id` (`brand_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

-- ----------------------------
-- Table structure for pms_spu_spec_relation (SPU规格选用关联表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_spu_spec_relation`;
CREATE TABLE `pms_spu_spec_relation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `spu_id` BIGINT NOT NULL COMMENT 'SPU ID',
    `spec_key_id` BIGINT NOT NULL COMMENT '规格项ID',
    `spec_value_id` BIGINT NOT NULL COMMENT '规格值ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_spec_key_id` (`spec_key_id`),
    KEY `idx_spec_value_id` (`spec_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU规格选用关联表';

-- ----------------------------
-- Table structure for pms_sku (商品SKU表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_sku`;
CREATE TABLE `pms_sku` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
    `spu_id` BIGINT NOT NULL COMMENT 'SPU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU编码/条形码',
    `name` VARCHAR(255) NOT NULL COMMENT 'SKU名称/描述',
    `pic` VARCHAR(500) DEFAULT NULL COMMENT 'SKU专属图片',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '销售价格',
    `original_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '市场原价/划线价',
    `cost_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '成本价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '当前库存',
    `lock_stock` INT NOT NULL DEFAULT 0 COMMENT '锁定/预占库存',
    `weight` DECIMAL(8,2) DEFAULT 0.00 COMMENT '重量 (kg)',
    `volume` DECIMAL(8,2) DEFAULT 0.00 COMMENT '体积 (m³)',
    `spec_data` TEXT DEFAULT NULL COMMENT '多规格属性快照 (JSON 冗余)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

-- ----------------------------
-- Table structure for pms_sku_spec_value (SKU规格绑定关系表)
-- ----------------------------
DROP TABLE IF EXISTS `pms_sku_spec_value`;
CREATE TABLE `pms_sku_spec_value` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
    `spu_id` BIGINT NOT NULL COMMENT 'SPU ID',
    `spec_key_id` BIGINT NOT NULL COMMENT '规格项ID',
    `spec_key_name` VARCHAR(64) NOT NULL COMMENT '规格项名称 (冗余快照)',
    `spec_value_id` BIGINT NOT NULL COMMENT '规格值ID',
    `spec_value` VARCHAR(128) NOT NULL COMMENT '规格值 (冗余快照)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sku_id` (`sku_id`),
    KEY `idx_spu_id` (`spu_id`),
    KEY `idx_spec_value_id` (`spec_value_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU规格绑定关系表';

-- 预置SPU与多规格SKU测试数据
INSERT INTO `pms_spu` (`id`, `shop_id`, `name`, `spu_code`, `category_id`, `brand_id`, `title`, `description`, `main_pic`, `slider_pics`, `spec_type`, `min_price`, `max_price`, `total_stock`, `unit`, `status`, `sort`) VALUES
(1, 1, 'Apple iPhone 15 Pro', 'SPU-IPHONE15P', 3, 1, '钛金属设计，A17 Pro 芯片，4800万像素主摄', '<p>iPhone 15 Pro 全新登场，强悍性能与钛金属机身。</p>', 'https://example.com/product/iphone15pro/main.jpg', '["https://example.com/product/iphone15pro/1.jpg","https://example.com/product/iphone15pro/2.jpg"]', 1, 7999.00, 8999.00, 200, '台', 1, 1);

-- SPU 关联选用的规格值
INSERT INTO `pms_spu_spec_relation` (`spu_id`, `spec_key_id`, `spec_value_id`) VALUES
(1, 1, 1),
(1, 1, 2),
(1, 2, 3),
(1, 2, 4);

-- SKU 记录
INSERT INTO `pms_sku` (`id`, `spu_id`, `sku_code`, `name`, `pic`, `price`, `original_price`, `cost_price`, `stock`, `lock_stock`, `weight`, `volume`, `spec_data`, `status`) VALUES
(1, 1, 'SKU-IP15P-TIT-128', 'Apple iPhone 15 Pro 原色钛金属 128GB', 'https://example.com/product/iphone15pro/tit.jpg', 7999.00, 8999.00, 6500.00, 50, 0, 0.18, 0.001, '[{"specKeyId":1,"specKeyName":"机身颜色","specValueId":1,"specValue":"原色钛金属"},{"specKeyId":2,"specKeyName":"存储容量","specValueId":3,"specValue":"128GB"}]', 1),
(2, 1, 'SKU-IP15P-TIT-256', 'Apple iPhone 15 Pro 原色钛金属 256GB', 'https://example.com/product/iphone15pro/tit.jpg', 8999.00, 9999.00, 7500.00, 50, 0, 0.18, 0.001, '[{"specKeyId":1,"specKeyName":"机身颜色","specValueId":1,"specValue":"原色钛金属"},{"specKeyId":2,"specKeyName":"存储容量","specValueId":4,"specValue":"256GB"}]', 1),
(3, 1, 'SKU-IP15P-BLK-128', 'Apple iPhone 15 Pro 暗夜黑 128GB', 'https://example.com/product/iphone15pro/blk.jpg', 7999.00, 8999.00, 6500.00, 50, 0, 0.18, 0.001, '[{"specKeyId":1,"specKeyName":"机身颜色","specValueId":2,"specValue":"暗夜黑"},{"specKeyId":2,"specKeyName":"存储容量","specValueId":3,"specValue":"128GB"}]', 1),
(4, 1, 'SKU-IP15P-BLK-256', 'Apple iPhone 15 Pro 暗夜黑 256GB', 'https://example.com/product/iphone15pro/blk.jpg', 8999.00, 9999.00, 7500.00, 50, 0, 0.18, 0.001, '[{"specKeyId":1,"specKeyName":"机身颜色","specValueId":2,"specValue":"暗夜黑"},{"specKeyId":2,"specKeyName":"存储容量","specValueId":4,"specValue":"256GB"}]', 1);

-- SKU 与规格值绑定关系
INSERT INTO `pms_sku_spec_value` (`sku_id`, `spu_id`, `spec_key_id`, `spec_key_name`, `spec_value_id`, `spec_value`) VALUES
(1, 1, 1, '机身颜色', 1, '原色钛金属'),
(1, 1, 2, '存储容量', 3, '128GB'),
(2, 1, 1, '机身颜色', 1, '原色钛金属'),
(2, 1, 2, '存储容量', 4, '256GB'),
(3, 1, 1, '机身颜色', 2, '暗夜黑'),
(3, 1, 2, '存储容量', 3, '128GB'),
(4, 1, 1, '机身颜色', 2, '暗夜黑'),
(4, 1, 2, '存储容量', 4, '256GB');

-- ----------------------------
-- Table structure for oms_order (订单主表)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单流水号 (全局唯一)',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品总金额',
    `freight_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
    `pay_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '应付/实付总金额',
    `pay_type` TINYINT NOT NULL DEFAULT 0 COMMENT '支付方式: 0-未支付, 1-支付宝, 2-微信支付, 3-银联, 4-余额支付',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款, 1-待发货, 2-已发货, 3-已完成, 4-已取消, 5-已关闭',
    `receiver_name` VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(32) NOT NULL COMMENT '收货人电话',
    `receiver_province` VARCHAR(64) DEFAULT NULL COMMENT '省份/直辖市',
    `receiver_city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `receiver_district` VARCHAR(64) DEFAULT NULL COMMENT '区/县',
    `receiver_detail_address` VARCHAR(255) NOT NULL COMMENT '详细收货地址',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '买家留言/订单备注',
    `delivery_company` VARCHAR(64) DEFAULT NULL COMMENT '物流配送公司 (如: 顺丰速运, 中通快递)',
    `delivery_sn` VARCHAR(64) DEFAULT NULL COMMENT '物流快递单号',
    `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方支付交易流水号',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `receive_time` DATETIME DEFAULT NULL COMMENT '确认收货时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因说明',
    `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '运营内部备注',
    `admin_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '运营插旗标色: 0-无, 1-红, 2-黄, 3-绿, 4-蓝, 5-紫',
    `delete_status` TINYINT NOT NULL DEFAULT 0 COMMENT '用户逻辑删除: 0-未删除, 1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_sn` (`order_sn`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- ----------------------------
-- Table structure for oms_order_item (订单明细项与商品快照表)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '所属订单ID',
    `order_sn` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `spu_id` BIGINT NOT NULL COMMENT '商品 SPU ID',
    `spu_name` VARCHAR(128) NOT NULL COMMENT '商品名称 (下单快照)',
    `spu_pic` VARCHAR(500) DEFAULT NULL COMMENT '商品主图 (下单快照)',
    `sku_id` BIGINT NOT NULL COMMENT '商品 SKU ID',
    `sku_code` VARCHAR(64) NOT NULL COMMENT 'SKU 编码 (下单快照)',
    `sku_name` VARCHAR(255) NOT NULL COMMENT 'SKU 名称 (下单快照)',
    `sku_pic` VARCHAR(500) DEFAULT NULL COMMENT 'SKU 规格图片 (下单快照)',
    `sku_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '购买单价 (下单快照)',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `subtotal_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商品小计金额 (sku_price * quantity)',
    `spec_data` TEXT DEFAULT NULL COMMENT '多规格属性快照 JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_sn` (`order_sn`),
    KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细快照表';

-- 预置示例订单测试数据
INSERT INTO `oms_order` (`id`, `user_id`, `order_sn`, `total_amount`, `freight_amount`, `pay_amount`, `pay_type`, `status`, `receiver_name`, `receiver_phone`, `receiver_province`, `receiver_city`, `receiver_district`, `receiver_detail_address`, `note`, `payment_time`)
VALUES (1, 1, 'ORD202608291200000001', 7999.00, 0.00, 7999.00, 2, 1, '张伟', '13800138000', '北京市', '北京市', '海淀区', '中关村南大街1号院8号楼', '请尽快安排顺丰发货', NOW());

INSERT INTO `oms_order_item` (`id`, `order_id`, `order_sn`, `spu_id`, `spu_name`, `spu_pic`, `sku_id`, `sku_code`, `sku_name`, `sku_pic`, `sku_price`, `quantity`, `subtotal_amount`, `spec_data`)
VALUES (1, 1, 'ORD202608291200000001', 1, 'Apple iPhone 15 Pro', 'https://example.com/product/iphone15pro/main.jpg', 1, 'SKU-IP15P-TIT-128', 'Apple iPhone 15 Pro 原色钛金属 128GB', 'https://example.com/product/iphone15pro/tit.jpg', 7999.00, 1, 7999.00, '[{"specKeyName":"机身颜色","specValue":"原色钛金属"},{"specKeyName":"存储容量","specValue":"128GB"}]');

-- ----------------------------
-- Table structure for sms_shop (店铺主表)
-- ----------------------------
DROP TABLE IF EXISTS `sms_shop`;
CREATE TABLE `sms_shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
    `user_id` BIGINT NOT NULL COMMENT '店主用户ID',
    `cert_id` BIGINT DEFAULT NULL COMMENT '关联认证记录ID (sys_user_certification.id)',
    `name` VARCHAR(128) NOT NULL COMMENT '店铺名称',
    `logo` VARCHAR(500) DEFAULT NULL COMMENT '店铺 Logo 图标',
    `banner` VARCHAR(500) DEFAULT NULL COMMENT '店铺横幅大图 / 门头招牌',
    `intro` VARCHAR(500) DEFAULT NULL COMMENT '店铺简介',
    `notice` VARCHAR(500) DEFAULT NULL COMMENT '店铺公告',
    `phone` VARCHAR(32) NOT NULL COMMENT '客服/联系电话',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '店铺类型: 1-个人店, 2-个体工商户店, 3-企业旗舰店, 4-企业专营店',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '店铺状态: 0-待审核, 1-正常营业, 2-暂停营业(打烊), 3-审核驳回, 4-违规封禁',
    `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核驳回原因',
    `score` DECIMAL(3,2) NOT NULL DEFAULT 5.00 COMMENT '店铺评分 (1.00-5.00)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_shop_name` (`name`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺主表';

-- 预置示例店铺数据 (用户1 admin 的官方自营旗舰店)
INSERT INTO `sms_shop` (`id`, `user_id`, `cert_id`, `name`, `logo`, `banner`, `intro`, `notice`, `phone`, `type`, `status`, `score`)
VALUES (1, 1, 1, '极光数码官方旗舰店', 'https://example.com/shop/logo.png', 'https://example.com/shop/banner.png', '极光数码官方自营旗舰店，正品行货，全国联保。', '全场新品火热发售中，支持顺丰次日达！', '400-888-9999', 3, 1, 4.95);

-- ----------------------------
-- Table structure for sys_role (系统管理角色表)
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(64) NOT NULL COMMENT '角色名称 (如: 超级管理员, 运营专员, 财务主管)',
    `code` VARCHAR(64) NOT NULL COMMENT '角色标识 (如: SUPER_ADMIN, OPS, FINANCE)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `permissions` TEXT DEFAULT NULL COMMENT '权限标识JSON列表 (如: ["*:*:*"] 或 ["order:view", "shop:audit"])',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理角色表';

-- 预置基础角色
INSERT INTO `sys_role` (`id`, `name`, `code`, `description`, `permissions`, `status`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有平台所有运营管理权限', '["*:*:*"]', 1),
(2, '运营专员', 'OPS', '负责商品、店铺与订单日常审核调度', '["dashboard:view","user:view","user:status","shop:view","shop:audit","spu:view","spu:audit","order:view","order:deliver","order:remark"]', 1);

-- ----------------------------
-- Table structure for sys_admin_user (系统管理员表)
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_user`;
CREATE TABLE `sys_admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
    `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(255) NOT NULL COMMENT '密码哈希密文 (BCrypt)',
    `nickname` VARCHAR(64) NOT NULL COMMENT '管理员昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系手机',
    `role_id` BIGINT NOT NULL COMMENT '关联角色ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理员表';

-- 预置初始超级管理员账号: admin / admin123 (BCrypt hash)
INSERT INTO `sys_admin_user` (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `phone`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$wS2N1B4G6iS1F3zS7.9NGe9fKz8L8v.z7cR4eL2M3V0n1J5p6Q2e.', '平台总控运营', 'https://example.com/avatar/admin.png', 'admin@mall.com', '13800000000', 1, 1);

-- ----------------------------
-- Table structure for oms_order_refund (订单退款申请表)
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_refund`;
CREATE TABLE `oms_order_refund` (
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






