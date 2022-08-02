package com.ruyuan.eshop.order.statemachine.action.aftersale.node;

import com.ruyuan.eshop.order.domain.dto.ReleaseProductStockDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.domain.request.AuditPassReleaseAssetsRequest;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 售后审核 组装释放库存 节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AfterSaleBuildInventoryDataNode extends StandardProcessor {

    @Override
    protected void processInternal(ProcessContext processContext) {

        AfterSaleItemDO afterSaleItemDO = processContext.get("afterSaleItemDO");

        AuditPassReleaseAssetsRequest auditPassReleaseAssetsRequest = new AuditPassReleaseAssetsRequest();
        ReleaseProductStockDTO releaseProductStockDTO = new ReleaseProductStockDTO();
        List<ReleaseProductStockDTO.OrderItemRequest> orderItemDTOList = new ArrayList<>();
        ReleaseProductStockDTO.OrderItemRequest orderItemRequest = new ReleaseProductStockDTO.OrderItemRequest();

        orderItemRequest.setSkuCode(afterSaleItemDO.getSkuCode());
        orderItemRequest.setSaleQuantity(afterSaleItemDO.getReturnQuantity());
        orderItemDTOList.add(orderItemRequest);

        releaseProductStockDTO.setOrderId(afterSaleItemDO.getOrderId());
        releaseProductStockDTO.setOrderItemRequestList(orderItemDTOList);
        releaseProductStockDTO.setSkuCode(afterSaleItemDO.getSkuCode());
        auditPassReleaseAssetsRequest.setReleaseProductStockDTO(releaseProductStockDTO);

        processContext.set("auditPassReleaseAssetsRequest", auditPassReleaseAssetsRequest);
    }
}
