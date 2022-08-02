package com.ruyuan.eshop.order.elasticsearch.handler.order;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.OrderDeliveryDetailDAO;
import com.ruyuan.eshop.order.domain.entity.OrderDeliveryDetailDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsOrderDeliveryDetail;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单配送信息更新service：
 * <p>
 * 当order_delivery_被更新到时候，只需要更新es中订单数据即可
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsOrderDeliveryDetailUpdateHandler extends EsAbstractHandler {

    @Autowired
    private OrderDeliveryDetailDAO orderDeliveryDetailDAO;

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private EsOrderListQueryIndexHandler esOrderListQueryIndexHandler;

    @Autowired
    private EsEntityConverter esEntityConverter;

    /**
     * 将订单配送信息同步至es
     */
    public void sync(List<String> orderIds, long timestamp) throws Exception {
        // 1、查询订单配送信息
        List<OrderDeliveryDetailDO> orderDeliveryDetails = orderDeliveryDetailDAO.listByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(orderDeliveryDetails)) {
            return;
        }

        // 2、将订单配送记录同步到es里面去
        List<EsOrderDeliveryDetail> esOrderDeliveryDetails = esEntityConverter.convertToEsOrderDeliveryDetails(orderDeliveryDetails);
        setEsIdOfOrderDeliveryDetail(esOrderDeliveryDetails);
        esClientService.bulkIndex(esOrderDeliveryDetails);

        // 3、异步构建OrderListQueryListIndex并同步到es
        esOrderListQueryIndexHandler.asyncBuildAndSynToEs(orderIds,timestamp);
    }
}
