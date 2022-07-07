DROP TABLE IF EXISTS wms_delivery_order;
CREATE TABLE wms_delivery_order (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `business_identifier` tinyint(4) NOT NULL COMMENT '接入方业务线标识  1, "自营商城"',
  `delivery_order_id` varchar(50) NOT NULL COMMENT '出库单ID',
  `order_id` varchar(50) NOT NULL COMMENT '订单ID',
  `seller_id` varchar(50) DEFAULT NULL COMMENT '卖家编号',
  `user_id` varchar(50) DEFAULT NULL COMMENT '买家编号',
  `pay_type` tinyint(4) DEFAULT NULL COMMENT '支付方式 10:微信支付 20:支付宝支付',
  `pay_amount` int(11) DEFAULT NULL COMMENT '交易支付金额',
  `total_amount` int(11) DEFAULT NULL COMMENT '交易总金额',
  `delivery_amount` int(11) DEFAULT NULL COMMENT '运费',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_delivery_order_id` (`delivery_order_id`)
) ENGINE=InnoDB COMMENT='出库单';

DROP TABLE IF EXISTS wms_delivery_order_item;
CREATE TABLE wms_delivery_order_item (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `delivery_order_id` varchar(50) NOT NULL COMMENT '出库单ID',
  `sku_code` varchar(50) NOT NULL COMMENT 'sku编码',
  `product_name` varchar(50) NOT NULL COMMENT '商品名称',
  `sale_quantity` int(11) NOT NULL COMMENT '销售数量',
  `sale_price` int(11) NOT NULL COMMENT '销售单价',
  `origin_amount` int(11) NOT NULL COMMENT '当前商品支付原总价',
  `pay_amount` int(11) NOT NULL COMMENT '交易支付金额',
  `product_unit` varchar(10) NOT NULL COMMENT '商品单位',
  `picking_count` int(11) NOT NULL COMMENT '拣货数量',
  `sku_container_id` int(11) NOT NULL COMMENT '捡货仓库货柜ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_delivery_item_order_id` (`delivery_order_id`)
) ENGINE=InnoDB COMMENT='订单履约条目';
