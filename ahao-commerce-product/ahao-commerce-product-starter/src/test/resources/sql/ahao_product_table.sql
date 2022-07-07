DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku`
(
    `id`             bigint(20)     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`     varchar(50)    NOT NULL COMMENT '商品编号',
    `product_type`   tinyint(4)     NOT NULL COMMENT '商品类型 1:普通商品,2:预售商品',
    `sku_code`       varchar(50)    NOT NULL COMMENT '商品SKU编码',
    `product_name`   varchar(50)    NOT NULL COMMENT '商品名称',
    `product_img`    varchar(255)            DEFAULT NULL COMMENT '商品图片',
    `product_unit`   varchar(10)    NOT NULL COMMENT '商品单位',
    `sale_price`     decimal(18, 6) NOT NULL DEFAULT NULL COMMENT '销售价格',
    `purchase_price` decimal(18, 6) NOT NULL COMMENT '商品采购价格',
    `create_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sku_code` (`sku_code`)
) ENGINE = InnoDB COMMENT ='商品sku记录表';

INSERT INTO `product_sku` VALUES (1, '1001010', 1, '10101010', '测试商品', 'test.img', '个', 1000, 500, '2021-11-23 17:44:47', '2021-11-23 17:44:49');
INSERT INTO `product_sku` VALUES (2, '1001011', 1, '10101011', 'demo商品', 'demo.img', '瓶', 100, 50, '2021-11-23 17:45:31', '2021-11-23 17:45:34');

