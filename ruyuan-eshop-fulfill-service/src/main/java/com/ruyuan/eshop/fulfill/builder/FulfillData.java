package com.ruyuan.eshop.fulfill.builder;

import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillItemDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillLogDO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单履约数据构造器
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class FulfillData {

    /**
     * 履约单
     */
    private List<OrderFulfillDO> orderFulFills = new ArrayList<>();

    /**
     * 订单履约条目
     */
    private List<OrderFulfillItemDO> orderFulFillItems = new ArrayList<>();

    private List<OrderFulfillLogDO> orderFulfillLogs = new ArrayList<>();

    public void addOrderFulfill(OrderFulfillDO orderFulfill) {
        this.orderFulFills.add(orderFulfill);
    }

    public void addOrderFulFillItem(OrderFulfillItemDO orderFulfillItem) {
        this.orderFulFillItems.add(orderFulfillItem);
    }

    public void addOrderFulFillItems(List<OrderFulfillItemDO> orderFulFillItems) {
        this.orderFulFillItems.addAll(orderFulFillItems);
    }

    public void addOrderFulFillLog(OrderFulfillLogDO orderFulFillLog) {
        this.orderFulfillLogs.add(orderFulFillLog);
    }
}
