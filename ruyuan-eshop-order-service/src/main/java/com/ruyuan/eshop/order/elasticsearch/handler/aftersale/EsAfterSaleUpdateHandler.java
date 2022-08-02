package com.ruyuan.eshop.order.elasticsearch.handler.aftersale;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.AfterSaleInfoDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsAfterSaleInfo;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 售后单更新service：
 * <p>
 * 当after_sale_info被更新到时候，只需要更新es中订单数据即可
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsAfterSaleUpdateHandler extends EsAbstractHandler {

    @Autowired
    private AfterSaleInfoDAO afterSaleInfoDAO;

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private EsEntityConverter esEntityConverter;

    @Autowired
    private EsAfterSaleListQueryIndexHandler esAfterSaleListQueryIndexHandler;

    /**
     * 将售后单同步至es
     */
    public void sync(List<String> afterSaleIds, long timestamp) throws Exception {
        // 1、查询售后单
        List<AfterSaleInfoDO> afterSales = afterSaleInfoDAO.listByAfterSaleIds(afterSaleIds);
        if (CollectionUtils.isEmpty(afterSales)) {
            return;
        }

        // 2、将售后单据同步到es里面去
        List<EsAfterSaleInfo> esAfterSaleInfos = esEntityConverter.convertToEsAfterSaleInfos(afterSales);
        setEsIdOfAfterSaleInfo(esAfterSaleInfos);
        esClientService.bulkIndex(esAfterSaleInfos);

        // 3、异步构建OrderQueryListIndex，并同步到es里面去
        esAfterSaleListQueryIndexHandler.asyncBuildAndSynToEs(afterSales, afterSaleIds, timestamp);
    }
}
