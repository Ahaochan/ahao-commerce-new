package com.ruyuan.eshop.order.elasticsearch.handler.aftersale;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.AfterSaleItemDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsAfterSaleItem;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 售后单条目信息更新service：
 * <p>
 * 当after_sale_item被更新到时候，只需要更新es中订单数据即可
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsAfterSaleItemUpdateHandler extends EsAbstractHandler {

    @Autowired
    private AfterSaleItemDAO afterSaleItemDAO;

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

        // 1、查询售后单条目
        List<AfterSaleItemDO> afterSaleItems = afterSaleItemDAO.listByAfterSaleIds(afterSaleIds);
        if (CollectionUtils.isEmpty(afterSaleItems)) {
            return;
        }
        // 转化为es entity
        List<EsAfterSaleItem> esAfterSaleItems = esEntityConverter.convertToEsAfterSaleItems(afterSaleItems);
        // 设置esId
        setEsIdOfAfterSaleItem(esAfterSaleItems);

        // 3、将售后单条目同步到es里面去
        esClientService.bulkIndex(esAfterSaleItems);

        // 4、异步构建AfterSaleListQueryListIndex并同步到es
        esAfterSaleListQueryIndexHandler.asyncBuildAndSynToEs(afterSaleIds, timestamp);
    }
}
