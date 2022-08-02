package com.ruyuan.eshop.order.elasticsearch.handler.order;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.*;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.*;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import com.ruyuan.eshop.order.elasticsearch.query.OrderListQueryIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单全量数据新增service：
 * <p>
 * 当order_info被创建时（下单），会同时创建
 * 1.order_info
 * 2.order_item
 * 3.order_delivery_detail
 * 4.order_payment_detail
 * 5.order_amount
 * 6.order_amount_detail
 * 7.order_operate_log(保存到mongoDB)
 * 8.order_snapshot(保存到hbase)
 * <p>
 * 于是在监听到order_info的新增binlog日志时，需要将1～6的数据同步到es里面去
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsOrderFullDataAddHandler extends EsAbstractHandler {

    @Autowired
    private OrderInfoDAO orderInfoDAO;

    @Autowired
    private OrderItemDAO orderItemDAO;

    @Autowired
    private OrderDeliveryDetailDAO orderDeliveryDetailDAO;

    @Autowired
    private OrderPaymentDetailDAO orderPaymentDetailDAO;

    @Autowired
    private OrderAmountDAO orderAmountDAO;

    @Autowired
    private OrderAmountDetailDAO orderAmountDetailDAO;

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private EsOrderListQueryIndexHandler esOrderListQueryIndexHandler;

    @Autowired
    private EsEntityConverter esEntityConverter;


    /**
     * 将订单全量数据新增至es
     */
    public void sync(List<String> orderIds, long timestamp) throws Exception {

        // 查询订单
        List<OrderInfoDO> orders = orderInfoDAO.listByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }
        sync(orders, orderIds, timestamp);
    }


    /**
     * 将订单全量数据新增至es
     */
    public void sync(List<OrderInfoDO> orders, List<String> orderIds, long timestamp) throws Exception {
        // 1、order转化为es entity
        List<EsOrderInfo> esOrders = esEntityConverter.convertToEsOrderInfos(orders);
        setEsIdOfOrderInfo(esOrders);
        List<Object> esOrderFullData = new ArrayList<>();

        // 2、查询订单条目
        List<OrderItemDO> orderItems = orderItemDAO.listByOrderIds(orderIds);
        // 转化为es entity
        List<EsOrderItem> esOrderItems = esEntityConverter.convertToEsOrderItems(orderItems);
        setEsIdOfOrderItem(esOrderItems);
        esOrderFullData.addAll(esOrderItems);

        // 3、查询订单配送信息
        List<OrderDeliveryDetailDO> orderDeliveryDetails = orderDeliveryDetailDAO.listByOrderIds(orderIds);
        // 转化为es entity
        List<EsOrderDeliveryDetail> esOrderDeliveryDetails =
                esEntityConverter.convertToEsOrderDeliveryDetails(orderDeliveryDetails);
        setEsIdOfOrderDeliveryDetail(esOrderDeliveryDetails);
        esOrderFullData.addAll(esOrderDeliveryDetails);

        // 4、查询订单支付信息
        List<OrderPaymentDetailDO> orderPaymentDetails = orderPaymentDetailDAO.listByOrderIds(orderIds);
        // 转化为es entity
        List<EsOrderPaymentDetail> esOrderPaymentDetails =
                esEntityConverter.convertToEsOrderPaymentDetails(orderPaymentDetails);
        setEsIdOfOrderPaymentDetail(esOrderPaymentDetails);
        esOrderFullData.addAll(esOrderPaymentDetails);

        // 5、查询订单价格明细
        List<OrderAmountDetailDO> orderAmountDetails = orderAmountDetailDAO.listByOrderIds(orderIds);
        // 转化为es entity
        List<EsOrderAmountDetail> esOrderAmountDetails = esEntityConverter.convertToEsOrderAmountDetails(orderAmountDetails);
        setEsIdOfOrderAmountDetail(esOrderAmountDetails);
        esOrderFullData.addAll(esOrderAmountDetails);

        // 6、查询订单价格
        List<OrderAmountDO> orderAmounts = orderAmountDAO.listByOrderIds(orderIds);
        // 转化为es entity
        List<EsOrderAmount> esOrderAmounts = esEntityConverter.convertToEsOrderAmounts(orderAmounts);
        setEsIdOfOrderAmount(esOrderAmounts);
        esOrderFullData.addAll(esOrderAmounts);

        // 7、将订单全量数据同步到es里面去，会把订单明细数据全量的备份一份到ES里去，这块其实是不可取的
        // bad case，一般来说是不能这么去玩儿的
        esClientService.bulkIndex(esOrderFullData);

        // 8、构建orderListQueryIndex并同步到es
        // 订单分页查询索引，订单分页查询，要是去进行多表关联的，订单、订单配送、订单条目、订单支付，很多数据是要进行多表关联的
        // 再根据一堆的条件进行复杂的搜索和查询
        // 分页
        // 专门用来进行搜索的索引，索引是一个大宽表，我们多个表里所有用于搜索的字段，都要放到这个索引里去
        List<OrderListQueryIndex> orderListQueryIndices =
                esOrderListQueryIndexHandler.buildOrderListQueryIndex(orders, orderDeliveryDetails, orderItems, orderPaymentDetails);
        esOrderListQueryIndexHandler.sycToEs(orderListQueryIndices);
    }

}
