package com.ruyuan.eshop.order.elasticsearch.handler.order;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.constants.OrderQueryFiledNameConstant;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.OrderPaymentDetailDAO;
import com.ruyuan.eshop.order.domain.entity.OrderPaymentDetailDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsOrderPaymentDetail;
import com.ruyuan.eshop.order.elasticsearch.enums.EsIndexNameEnum;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单配送信息更新service：
 * <p>
 * 当order_delivery_detail被更新到时候，只需要更新es中订单数据即可
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsOrderPaymentDetailUpdateHandler extends EsAbstractHandler {

    @Autowired
    private OrderPaymentDetailDAO orderPaymentDetailDAO;

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private EsOrderListQueryIndexHandler esOrderListQueryIndexHandler;

    @Autowired
    private EsEntityConverter esEntityConverter;

    /**
     * 将订单支付明细同步至es
     * <p>
     * 因为OrderPaymentDetailDO未指定docId，是es自动生成的，
     * 所以要实现更新，需要采用先删除，后新增的方式
     */
    public void sync(List<String> orderIds, long timestamp) throws Exception {
        // 1、查询订单支付明细
        List<OrderPaymentDetailDO> orderPaymentDetails = orderPaymentDetailDAO.listByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(orderPaymentDetails)) {
            return;
        }


        // 2、将订单支付明细同步到es里面去
        List<EsOrderPaymentDetail> esOrderPaymentDetails = esEntityConverter.convertToEsOrderPaymentDetails(orderPaymentDetails);
        setEsIdOfOrderPaymentDetail(esOrderPaymentDetails);
        esClientService.bulkIndex(esOrderPaymentDetails);

        // 3、异步构建OrderListQueryListIndex并同步到es
        esOrderListQueryIndexHandler.asyncBuildAndSynToEs(orderIds,timestamp);
    }
}
