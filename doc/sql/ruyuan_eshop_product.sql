CREATE
database if NOT EXISTS `ruyuan_eshop_product` default character set utf8 collate utf8_general_ci;
use
`ruyuan_eshop_product`;

SET NAMES utf8;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for product_sku
-- ----------------------------
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`     varchar(50) COLLATE utf8_bin NOT NULL COMMENT '商品编号',
    `product_type`   tinyint(4) NOT NULL COMMENT '商品类型 1:普通商品,2:预售商品',
    `sku_code`       varchar(50) COLLATE utf8_bin NOT NULL COMMENT '商品SKU编码',
    `product_name`   varchar(50) COLLATE utf8_bin NOT NULL COMMENT '商品名称',
    `product_img`    varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '商品图片',
    `product_unit`   varchar(10) COLLATE utf8_bin NOT NULL COMMENT '商品单位',
    `sale_price`     int(10) DEFAULT NULL COMMENT '销售价格',
    `purchase_price` int(10) NOT NULL COMMENT '商品采购价格',
    `gmt_create`     datetime                     NOT NULL COMMENT '创建时间',
    `gmt_modified`   datetime                     NOT NULL COMMENT '更新时间',
    `ext_json`       varchar(1024)                 DEFAULT NULL COMMENT '扩展信息',
    PRIMARY KEY (`id`) USING BTREE,
    KEY              `idx_sku_code` (`sku_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='商品sku记录表'

-- ----------------------------
-- Records of product_sku
-- ----------------------------
BEGIN;

INSERT INTO ruyuan_eshop_product.product_sku (id, product_id, product_type, sku_code, product_name, product_img, product_unit, sale_price, purchase_price, gmt_create, gmt_modified, ext_json) VALUES (1, '1001010', 1, '10101010', '测试商品', 'test.img', '个', 1000, 500, '2021-11-23 17:44:47', '2021-11-23 17:44:49', null);
INSERT INTO ruyuan_eshop_product.product_sku (id, product_id, product_type, sku_code, product_name, product_img, product_unit, sale_price, purchase_price, gmt_create, gmt_modified, ext_json) VALUES (2, '1001011', 1, '10101011', 'demo商品', 'demo.img', '瓶', 100, 50, '2021-11-23 17:45:31', '2021-11-23 17:45:34', null);
INSERT INTO ruyuan_eshop_product.product_sku (id, product_id, product_type, sku_code, product_name, product_img, product_unit, sale_price, purchase_price, gmt_create, gmt_modified, ext_json) VALUES (3, '1001012', 2, '10101012', '虚拟商品', 'xuni.img', '张', 100, 50, '2021-11-23 17:45:31', '2021-11-23 17:45:34', null);
INSERT INTO ruyuan_eshop_product.product_sku (id, product_id, product_type, sku_code, product_name, product_img, product_unit, sale_price, purchase_price, gmt_create, gmt_modified, ext_json) VALUES (4, '1001013', 3, '10101013', '预售商品', 'yushou.img', '件', 100, 50, '2021-11-23 17:45:31', '2021-11-23 17:45:34', '2021-1-12 17:45:31');
COMMIT;

SET
FOREIGN_KEY_CHECKS = 1;
