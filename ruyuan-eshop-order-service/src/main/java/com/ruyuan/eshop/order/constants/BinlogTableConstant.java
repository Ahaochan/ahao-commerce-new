package com.ruyuan.eshop.order.constants;

/**
 * mysql binlog监听表名常量
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public class BinlogTableConstant {

    /**
     * 正向三张表：
     * order_info、order_delivery_detail、order_payment_detail
     */

    public static final String ORDER_INFO = "order_info";

    public static final String ORDER_DELIVERY_DETAIL = "order_delivery_detail";

    public static final String ORDER_PAYMENT_DETAIL = "order_payment_detail";

    /**
     * 逆向三张表：
     * after_sale_info、after_sale_item、after_sale_refund
     */
    public static final String AFTER_SALE_INFO = "after_sale_info";

    public static final String AFTER_SALE_ITEM = "after_sale_item";

    public static final String AFTER_SALE_REFUND = "after_sale_refund";

}
