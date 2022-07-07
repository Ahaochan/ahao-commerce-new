DROP TABLE IF EXISTS tms_logistic_order;
CREATE TABLE tms_logistic_order (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_identifier` tinyint(4) NOT NULL COMMENT '接入方业务线标识  1, "自营商城"',
  `order_id` varchar(50) NOT NULL COMMENT '订单ID',
  `seller_id` varchar(50) DEFAULT NULL COMMENT '卖家编号',
  `user_id` varchar(50) DEFAULT NULL COMMENT '买家编号',
  `logistic_code` varchar(50) NOT NULL COMMENT '物流单号',
  `content` varchar(1024) NOT NULL COMMENT '物流单内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_logistic_code` (`logistic_code`)
) ENGINE=InnoDB COMMENT='物流单';
