package com.ruyuan.eshop.order.elasticsearch.handler.aftersale;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.AfterSaleRefundDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsAfterSaleRefund;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 售后单退款信息更新service：
 * <p>
 * 当after_sale_refund被更新到时候，只需要更新es中订单数据即可
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsAfterSaleRefundUpdateHandler extends EsAbstractHandler {

    @Autowired
    private AfterSaleRefundDAO afterSaleRefundDAO;

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private EsEntityConverter esEntityConverter;

    @Autowired
    private EsAfterSaleListQueryIndexHandler esAfterSaleListQueryIndexHandler;

    /**
     * 将售后单条目同步至es
     */
    public void sync(List<String> afterSaleIds, long timestamp) throws Exception {

        // 1、查询售后退款信息
        List<AfterSaleRefundDO> afterSaleRefunds = afterSaleRefundDAO.listByAfterSaleIds(afterSaleIds);
        if (CollectionUtils.isEmpty(afterSaleRefunds)) {
            return;
        }
        // 2、将售后单条目同步到es里面去
        List<EsAfterSaleRefund> esAfterSaleRefunds = esEntityConverter.convertToEsAfterSaleRefunds(afterSaleRefunds);
        setEsIdOfAfterSaleRefund(esAfterSaleRefunds);
        esClientService.bulkIndex(esAfterSaleRefunds);

        // 4、异步构建AfterSaleListQueryListIndex并同步到es
        esAfterSaleListQueryIndexHandler.asyncBuildAndSynToEs(afterSaleIds, timestamp);
    }

}
