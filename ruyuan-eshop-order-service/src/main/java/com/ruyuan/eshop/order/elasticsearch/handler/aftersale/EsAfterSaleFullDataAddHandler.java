package com.ruyuan.eshop.order.elasticsearch.handler.aftersale;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.order.converter.EsEntityConverter;
import com.ruyuan.eshop.order.dao.AfterSaleInfoDAO;
import com.ruyuan.eshop.order.dao.AfterSaleItemDAO;
import com.ruyuan.eshop.order.dao.AfterSaleRefundDAO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
import com.ruyuan.eshop.order.elasticsearch.EsClientService;
import com.ruyuan.eshop.order.elasticsearch.entity.EsAfterSaleInfo;
import com.ruyuan.eshop.order.elasticsearch.entity.EsAfterSaleItem;
import com.ruyuan.eshop.order.elasticsearch.entity.EsAfterSaleRefund;
import com.ruyuan.eshop.order.elasticsearch.handler.EsAbstractHandler;
import com.ruyuan.eshop.order.elasticsearch.query.AfterSaleListQueryIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 售后单全量数据新增service：
 * <p>
 * 当after_sale_info被创建时（下单），会同时创建
 * 1.after_sale_info
 * 2.after_sale_item
 * 3.after_sale_refund
 * <p>
 * 于是在监听到after_sale_info的新增binlog日志时，需要将1～3的数据同步到es里面去
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
@Slf4j
public class EsAfterSaleFullDataAddHandler extends EsAbstractHandler {

    @Autowired
    private AfterSaleInfoDAO afterSaleInfoDAO;

    @Autowired
    private AfterSaleItemDAO afterSaleItemDAO;

    @Autowired
    private AfterSaleRefundDAO afterSaleRefundDAO;

    @Autowired
    private EsClientService esClientService;

    @Autowired
    private EsEntityConverter esEntityConverter;

    @Autowired
    private EsAfterSaleListQueryIndexHandler esAfterSaleListQueryIndexHandler;


    /**
     * 将售后单全量数据新增至es
     */
    public void sync(List<String> afterSaleIds, long timestamp) throws Exception {

        // 查询售后单
        List<AfterSaleInfoDO> afterSales = afterSaleInfoDAO.listByAfterSaleIds(afterSaleIds);
        if (CollectionUtils.isEmpty(afterSales)) {
            return;
        }

        sync(afterSales,afterSaleIds,timestamp);
    }

    /**
     * 将售后单全量数据新增至es
     */
    public void sync(List<AfterSaleInfoDO> afterSales, List<String> afterSaleIds, long timestamp) throws Exception {
        // 1、售后单转化为es entity
        List<EsAfterSaleInfo> esAfterSales = esEntityConverter.convertToEsAfterSaleInfos(afterSales);
        setEsIdOfAfterSaleInfo(esAfterSales);
        List<Object> esOrderFullData = new ArrayList<>(esAfterSales);

        // 2、查询售后单条目
        List<AfterSaleItemDO> afterSaleItems = afterSaleItemDAO.listByAfterSaleIds(afterSaleIds);
        // 转化为es entity
        List<EsAfterSaleItem> esAfterSaleItems = esEntityConverter.convertToEsAfterSaleItems(afterSaleItems);
        setEsIdOfAfterSaleItem(esAfterSaleItems);
        esOrderFullData.addAll(esAfterSaleItems);

        // 3、查询售后退款信息
        List<AfterSaleRefundDO> afterSaleRefunds = afterSaleRefundDAO.listByAfterSaleIds(afterSaleIds);
        // 转化为es entity
        List<EsAfterSaleRefund> esAfterSaleRefunds = esEntityConverter.convertToEsAfterSaleRefunds(afterSaleRefunds);
        setEsIdOfAfterSaleRefund(esAfterSaleRefunds);
        esOrderFullData.addAll(esAfterSaleRefunds);

        // 4、将订单全量数据同步到es里面去
        esClientService.bulkIndex(esOrderFullData);

        // 5、构建afterSaleListQueryIndex并同步到es
        List<AfterSaleListQueryIndex> afterSaleListQueryIndices =
                esAfterSaleListQueryIndexHandler.buildAfterSaleListQueryIndex(afterSales, afterSaleItems, afterSaleRefunds);
        esAfterSaleListQueryIndexHandler.sycToEs(afterSaleListQueryIndices);
    }
}
