CREATE database if NOT EXISTS ahao_fulfill default character set utf8 collate utf8_general_ci;
use ahao_fulfill;

DROP TABLE IF EXISTS `order_fulfill`;
CREATE TABLE `order_fulfill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_identifier` tinyint(4) NOT NULL COMMENT '接入方业务线标识  1, "自营商城"',
  `fulfill_id` varchar(50) NOT NULL COMMENT '履约单号ID',
  `order_id` varchar(50) NOT NULL COMMENT '订单ID',
  `seller_id` varchar(50) DEFAULT NULL COMMENT '卖家编号',
  `user_id` varchar(50) DEFAULT NULL COMMENT '买家编号',
  `delivery_type` tinyint(4) DEFAULT NULL COMMENT '配送类型',
  `receiver_name` varchar(50) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(50) DEFAULT NULL COMMENT '收货人电话',
  `receiver_province` varchar(50) DEFAULT NULL COMMENT '省',
  `receiver_city` varchar(50) DEFAULT NULL COMMENT '市',
  `receiver_area` varchar(255) DEFAULT NULL COMMENT '区',
  `receiver_street` varchar(255) DEFAULT NULL COMMENT '街道地址',
  `receiver_detail_address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `receiver_lon` decimal(20,10) DEFAULT NULL COMMENT '经度',
  `receiver_lat` decimal(20,10) DEFAULT NULL COMMENT '维度',
  `deliverer_no` varchar(50) DEFAULT NULL COMMENT '配送员编号',
  `deliverer_name` varchar(50) DEFAULT NULL COMMENT '配送员姓名',
  `deliverer_phone` varchar(50) DEFAULT NULL COMMENT '配送员手机号',
  `logistics_code` varchar(50) DEFAULT NULL COMMENT '物流单号',
  `user_remark` varchar(255) DEFAULT NULL COMMENT '用户备注',
  `pay_type` tinyint(4) DEFAULT NULL COMMENT '支付方式 10:微信支付 20:支付宝支付',
  `pay_amount` int(11) DEFAULT NULL COMMENT '交易支付金额',
  `total_amount` int(11) DEFAULT NULL COMMENT '交易总金额',
  `delivery_amount` int(11) DEFAULT NULL COMMENT '运费',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ful_fill_id` (`fulfill_id`) USING BTREE
) ENGINE=InnoDB COMMENT='订单履约表';

DROP TABLE IF EXISTS `order_fulfill_item`;
CREATE TABLE `order_fulfill_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `fulfill_id` varchar(50) NOT NULL COMMENT '履约单号ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'sku编码',
  `product_name` varchar(50) NOT NULL COMMENT '商品名称',
  `sale_quantity` int(11) NOT NULL COMMENT '销售数量',
  `sale_price` int(11) NOT NULL COMMENT '销售单价',
  `origin_amount` int(11) NOT NULL COMMENT '当前商品支付原总价',
  `pay_amount` int(11) NOT NULL COMMENT '交易支付金额',
  `product_unit` varchar(10) NOT NULL COMMENT '商品单位',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_ful_fill_id` (`fulfill_id`) USING BTREE
) ENGINE=InnoDB COMMENT='订单履约条目表';

-- ----------------------------
-- https://github.com/seata/seata/blob/develop/script/client/saga/db/mysql.sql
-- ----------------------------
DROP TABLE IF EXISTS `seata_state_inst`;
CREATE TABLE `seata_state_inst` (
  `id` varchar(48) NOT NULL COMMENT 'id',
  `machine_inst_id` varchar(128) NOT NULL COMMENT 'state machine instance id',
  `NAME` varchar(128) NOT NULL COMMENT 'state name',
  `TYPE` varchar(20) DEFAULT NULL COMMENT 'state type',
  `service_name` varchar(128) DEFAULT NULL COMMENT 'service name',
  `service_method` varchar(128) DEFAULT NULL COMMENT 'method name',
  `service_type` varchar(16) DEFAULT NULL COMMENT 'service type',
  `business_key` varchar(48) DEFAULT NULL COMMENT 'business key',
  `state_id_compensated_for` varchar(50) DEFAULT NULL COMMENT 'state compensated for',
  `state_id_retried_for` varchar(50) DEFAULT NULL COMMENT 'state retried for',
  `gmt_started` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'start time',
  `is_for_update` tinyint(1) DEFAULT NULL COMMENT 'is service for update',
  `input_params` longtext COMMENT 'input parameters',
  `output_params` longtext COMMENT 'output parameters',
  `STATUS` varchar(2) NOT NULL COMMENT 'status(SU succeed|FA failed|UN unknown|SK skipped|RU running)',
  `excep` blob COMMENT 'exception',
  `gmt_end` timestamp(3) NULL DEFAULT NULL COMMENT 'end time',
  PRIMARY KEY (`id`,`machine_inst_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `seata_state_machine_def`;
CREATE TABLE `seata_state_machine_def` (
  `id` varchar(32) NOT NULL COMMENT 'id',
  `name` varchar(128) NOT NULL COMMENT 'name',
  `tenant_id` varchar(32) NOT NULL COMMENT 'tenant id',
  `app_name` varchar(32) NOT NULL COMMENT 'application name',
  `type` varchar(20) DEFAULT NULL COMMENT 'state language type',
  `comment_` varchar(255) DEFAULT NULL COMMENT 'comment',
  `ver` varchar(16) NOT NULL COMMENT 'version',
  `gmt_create` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'create time',
  `status` varchar(2) NOT NULL COMMENT 'status(AC:active|IN:inactive)',
  `content` longtext COMMENT 'content',
  `recover_strategy` varchar(16) DEFAULT NULL COMMENT 'transaction recover strategy(compensate|retry)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `seata_state_machine_inst`;
CREATE TABLE `seata_state_machine_inst` (
  `id` varchar(128) NOT NULL COMMENT 'id',
  `machine_id` varchar(32) NOT NULL COMMENT 'state machine definition id',
  `tenant_id` varchar(32) NOT NULL COMMENT 'tenant id',
  `parent_id` varchar(128) DEFAULT NULL COMMENT 'parent id',
  `gmt_started` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT 'start time',
  `business_key` varchar(48) DEFAULT NULL COMMENT 'business key',
  `start_params` longtext COMMENT 'start parameters',
  `gmt_end` timestamp(3) NULL DEFAULT NULL COMMENT 'end time',
  `excep` blob COMMENT 'exception',
  `end_params` longtext COMMENT 'end parameters',
  `STATUS` varchar(2) DEFAULT NULL COMMENT 'status(SU succeed|FA failed|UN unknown|SK skipped|RU running)',
  `compensation_status` varchar(2) DEFAULT NULL COMMENT 'compensation status(SU succeed|FA failed|UN unknown|SK skipped|RU running)',
  `is_running` tinyint(1) DEFAULT NULL COMMENT 'is running(0 no|1 yes)',
  `gmt_updated` timestamp(3) NULL DEFAULT NULL COMMENT 'gmt_updated',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `unikey_buz_tenant` (`business_key`,`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
