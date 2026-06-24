-- ============================================================
-- 旅游服务平台数据库初始化脚本
-- 数据库：travel_db
-- MySQL版本：5.7
-- ============================================================

CREATE DATABASE IF NOT EXISTS travel_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE travel_db;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_user` (
    `user_id`       INT(11)      NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`      VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar`        VARCHAR(200) DEFAULT '/images/default-avatar.png' COMMENT '头像',
    `gender`        TINYINT(1)   DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `birthday`      DATE         DEFAULT NULL COMMENT '生日',
    `user_type`     TINYINT(1)   DEFAULT 1 COMMENT '用户类型：1-普通用户 2-商家 3-管理员',
    `status`        TINYINT(1)   DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 景点表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_scenic` (
    `scenic_id`       INT(11)       NOT NULL AUTO_INCREMENT COMMENT '景点ID',
    `scenic_name`     VARCHAR(100)  NOT NULL COMMENT '景点名称',
    `city`            VARCHAR(50)   NOT NULL COMMENT '所在城市',
    `address`         VARCHAR(200)  NOT NULL COMMENT '详细地址',
    `longitude`       DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `latitude`        DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `description`     TEXT          COMMENT '景点描述',
    `open_time`       VARCHAR(100)  DEFAULT '全天' COMMENT '开放时间',
    `ticket_price`    DECIMAL(10,2) DEFAULT 0.00 COMMENT '门票价格',
    `recommend_level` TINYINT(1)    DEFAULT 3 COMMENT '推荐等级：1-5',
    `cover_image`     VARCHAR(200)  DEFAULT NULL COMMENT '封面图片',
    `images`          TEXT          COMMENT '景点图片JSON数组',
    `tags`            VARCHAR(200)  DEFAULT NULL COMMENT '标签（逗号分隔）',
    `view_count`      INT(11)       DEFAULT 0 COMMENT '浏览数',
    `status`          TINYINT(1)    DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`scenic_id`),
    KEY `idx_city` (`city`),
    KEY `idx_city_status` (`city`, `status`),
    KEY `idx_recommend` (`recommend_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点表';

-- ============================================================
-- 3. 酒店表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_hotel` (
    `hotel_id`    INT(11)       NOT NULL AUTO_INCREMENT COMMENT '酒店ID',
    `hotel_name`  VARCHAR(100)  NOT NULL COMMENT '酒店名称',
    `city`        VARCHAR(50)   NOT NULL COMMENT '所在城市',
    `address`     VARCHAR(200)  NOT NULL COMMENT '详细地址',
    `star_level`  TINYINT(1)    DEFAULT 3 COMMENT '星级：1-5',
    `phone`       VARCHAR(20)   DEFAULT NULL COMMENT '联系电话',
    `description` TEXT          COMMENT '酒店描述',
    `facilities`  VARCHAR(500)  DEFAULT NULL COMMENT '设施（逗号分隔）',
    `images`      TEXT          COMMENT '酒店图片JSON数组',
    `longitude`   DECIMAL(10,7) DEFAULT NULL,
    `latitude`    DECIMAL(10,7) DEFAULT NULL,
    `status`      TINYINT(1)    DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`hotel_id`),
    KEY `idx_city` (`city`),
    KEY `idx_city_star` (`city`, `star_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店表';

-- ============================================================
-- 4. 酒店房型表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_hotel_room` (
    `room_id`    INT(11)      NOT NULL AUTO_INCREMENT COMMENT '房型ID',
    `hotel_id`   INT(11)      NOT NULL COMMENT '酒店ID',
    `room_type`  VARCHAR(50)  NOT NULL COMMENT '房型名称',
    `bed_type`   VARCHAR(20)  DEFAULT '大床' COMMENT '床型',
    `facilities` VARCHAR(200) DEFAULT NULL COMMENT '房内设施',
    `price`      DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock`      INT(11)      DEFAULT 0 COMMENT '库存',
    `status`     TINYINT(1)   DEFAULT 1 COMMENT '状态：0-停售 1-在售',
    PRIMARY KEY (`room_id`),
    KEY `idx_hotel_id` (`hotel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='酒店房型表';

-- ============================================================
-- 5. 票务库存表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_ticket_inventory` (
    `inventory_id`    INT(11) NOT NULL AUTO_INCREMENT,
    `scenic_id`       INT(11) NOT NULL COMMENT '景点ID',
    `use_date`        DATE    NOT NULL COMMENT '使用日期',
    `total_stock`     INT(11) NOT NULL DEFAULT 0 COMMENT '总库存',
    `available_stock` INT(11) NOT NULL DEFAULT 0 COMMENT '可用库存',
    `version`         INT(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`inventory_id`),
    UNIQUE KEY `uk_scenic_date` (`scenic_id`, `use_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票务库存表';

-- ============================================================
-- 6. 订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order` (
    `order_id`      VARCHAR(32)  NOT NULL COMMENT '订单号（时间戳+随机数）',
    `user_id`       INT(11)      NOT NULL COMMENT '用户ID',
    `order_type`    TINYINT(1)   NOT NULL COMMENT '订单类型：1-酒店 2-景点门票',
    `total_amount`  DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount`    DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    `order_status`  TINYINT(1)   DEFAULT 1 COMMENT '状态：1-待支付 2-已支付 3-已取消 4-已完成',
    `pay_type`      TINYINT(1)   DEFAULT NULL COMMENT '支付方式：1-支付宝 2-微信',
    `pay_time`      DATETIME     DEFAULT NULL COMMENT '支付时间',
    `contact_name`  VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status_time` (`order_status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 7. 订单明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_item` (
    `item_id`       INT(11)      NOT NULL AUTO_INCREMENT,
    `order_id`      VARCHAR(32)  NOT NULL COMMENT '订单号',
    `product_type`  TINYINT(1)   NOT NULL COMMENT '产品类型：1-酒店 2-景点',
    `product_id`    INT(11)      NOT NULL COMMENT '产品ID',
    `product_name`  VARCHAR(100) NOT NULL COMMENT '产品名称',
    `quantity`      INT(11)      DEFAULT 1 COMMENT '数量',
    `price`         DECIMAL(10,2) NOT NULL COMMENT '单价',
    `use_date`      DATE         DEFAULT NULL COMMENT '使用日期',
    `refund_status` TINYINT(1)   DEFAULT 0 COMMENT '退款：0-无 1-申请中 2-已退款',
    PRIMARY KEY (`item_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product` (`product_type`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================================
-- 8. 游记表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_travel_note` (
    `note_id`       INT(11)      NOT NULL AUTO_INCREMENT COMMENT '游记ID',
    `user_id`       INT(11)      NOT NULL COMMENT '用户ID',
    `title`         VARCHAR(200) NOT NULL COMMENT '标题',
    `content`       LONGTEXT     NOT NULL COMMENT '内容（HTML格式）',
    `cover_image`   VARCHAR(200) DEFAULT NULL COMMENT '封面图片',
    `view_count`    INT(11)      DEFAULT 0 COMMENT '浏览数',
    `like_count`    INT(11)      DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT(11)      DEFAULT 0 COMMENT '评论数',
    `status`        TINYINT(1)   DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已删除',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`note_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游记表';

-- ============================================================
-- 9. 收藏表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_favorite` (
    `favorite_id` INT(11)   NOT NULL AUTO_INCREMENT,
    `user_id`     INT(11)   NOT NULL,
    `target_type` TINYINT(1) NOT NULL COMMENT '收藏类型：1-景点 2-酒店 3-游记',
    `target_id`   INT(11)   NOT NULL COMMENT '收藏目标ID',
    `create_time` DATETIME  DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`favorite_id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================================
-- 10. 评论表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_comment` (
    `comment_id`  INT(11)      NOT NULL AUTO_INCREMENT,
    `user_id`     INT(11)      NOT NULL COMMENT '用户ID',
    `target_type` TINYINT(1)   NOT NULL COMMENT '目标类型：1-景点 2-酒店 3-游记',
    `target_id`   INT(11)      NOT NULL COMMENT '目标ID',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `parent_id`   INT(11)      DEFAULT NULL COMMENT '父评论ID（回复）',
    `status`      TINYINT(1)   DEFAULT 1 COMMENT '状态：0-隐藏 1-正常',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`comment_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================================
-- 11. 系统配置表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_sys_config` (
    `config_id`    INT(11)      NOT NULL AUTO_INCREMENT,
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT         NOT NULL COMMENT '配置值',
    `remark`       VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================================
-- 12. 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_operation_log` (
    `log_id`       INT(11)      NOT NULL AUTO_INCREMENT,
    `user_id`      INT(11)      DEFAULT NULL COMMENT '用户ID',
    `operation`    VARCHAR(100) NOT NULL COMMENT '操作',
    `method`       VARCHAR(200) DEFAULT NULL COMMENT '方法',
    `params`       TEXT         COMMENT '参数',
    `ip`           VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    `execute_time` INT(11)      DEFAULT NULL COMMENT '执行时间(ms)',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================
-- 初始化数据
-- ============================================================
INSERT INTO `t_sys_config` (`config_key`, `config_value`, `remark`) VALUES
('system_name',       '旅游服务平台',                  '系统名称'),
('alipay_enabled',    '1',                           '支付宝支付是否启用'),
('wxpay_enabled',     '1',                           '微信支付是否启用'),
('amap_key',          'your_amap_key_here',          '高德地图Key'),
('hot_scenic_count',  '10',                          '热门景点显示数量'),
('upload_path',       '/upload/',                    '文件上传路径');

-- 插入测试管理员账号（密码 BCrypt 加密，明文: admin123）
INSERT INTO `t_user` (`username`, `password`, `nickname`, `user_type`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 3, 1);
