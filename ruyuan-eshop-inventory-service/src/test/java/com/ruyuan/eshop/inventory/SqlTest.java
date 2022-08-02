package com.ruyuan.eshop.inventory;

public class SqlTest {

    public static void main(String[] args) {
        prepareInventorySQL();
    }

    private static void prepareProductSQL() {
        for (int i = 1; i <= 100; i++) {
            StringBuilder sb = new StringBuilder("");
            String skuCode = "skuCode" + getNo(i);
            sb.append("INSERT INTO product_sku(product_id,product_type,sku_code,product_name,product_img,product_unit,sale_price,purchase_price,gmt_create,gmt_modified) VALUES (")
                    .append("'").append(skuCode).append("',")
                    .append("'").append(1).append("',")
                    .append("'").append(skuCode).append("',")
                    .append("'").append("压测数据" + getNo(i)).append("',")
                    .append("'").append("demo.img").append("',")
                    .append("'").append("个").append("',")
                    .append("'").append("1000").append("',")
                    .append("'").append("500").append("',")
                    .append("'").append("2021-12-17 11:02:25").append("',")
                    .append("'").append("2021-12-17 11:02:25").append("'")
                    .append(");");
            System.out.println(sb.toString());
        }
    }

    private static void prepareInventorySQL() {
        for (int i = 1; i <= 100; i++) {
            StringBuilder sb = new StringBuilder("");
            sb.append("INSERT INTO inventory_product_stock(sku_code,sale_stock_quantity,saled_stock_quantity,gmt_create,gmt_modified) VALUES (")
                    .append("'").append("skuCode" + getNo(i)).append("',")
                    .append("'").append(1000000000).append("',")
                    .append("'").append(0).append("',")
                    .append("'").append("2021-12-17 11:02:25").append("',")
                    .append("'").append("2021-12-17 11:02:25").append("'")
                    .append(");");
            System.out.println(sb.toString());
        }
    }

    private static String getNo(Integer i) {
        return String.valueOf(i);
    }
}
