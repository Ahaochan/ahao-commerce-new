package com.ruyuan.eshop.order.statemachine.action.order.create.node;

import com.ruyuan.consistency.annotation.ConsistencyTask;
import com.ruyuan.eshop.common.utils.LoggerFormat;
import com.ruyuan.eshop.inventory.domain.request.DeductProductStockRequest;
import com.ruyuan.eshop.inventory.domain.request.ReleaseProductStockRequest;
import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.domain.request.CreateOrderRequest;
import com.ruyuan.eshop.order.remote.InventoryRemote;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.RollbackProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class CreateOrderDeductStockNode extends RollbackProcessor {

    @Autowired
    private OrderConverter orderConverter;

    @Autowired
    private InventoryRemote inventoryRemote;

    @Override
    protected void rollback(ProcessContext processContext) {
        CreateOrderRequest createOrderRequest = processContext.get("createOrderRequest");
        ReleaseProductStockRequest releaseProductStockRequest = new ReleaseProductStockRequest();
        releaseProductStockRequest.setOrderId(createOrderRequest.getOrderId());
        List<ReleaseProductStockRequest.OrderItemRequest> orderItemRequestList =
                orderConverter.convertReleaseStockOrderItemRequests(createOrderRequest.getOrderItemRequestList());
        releaseProductStockRequest.setOrderItemRequestList(orderItemRequestList);

        doRollback(releaseProductStockRequest);
    }

    @Override
    protected void processInternal(ProcessContext processContext) {
        log.info(LoggerFormat.build()
                .remark("CreateOrderDeductStockNode.processInternal-> before deduct stock")
                .finish());
        // 扣减库存
        CreateOrderRequest createOrderRequest = processContext.get("createOrderRequest");
        String orderId = createOrderRequest.getOrderId();
        List<DeductProductStockRequest.OrderItemRequest> orderItemRequestList =
                orderConverter.convertDeductStockOrderItemRequests(createOrderRequest.getOrderItemRequestList());
        DeductProductStockRequest lockProductStockRequest = new DeductProductStockRequest();
        lockProductStockRequest.setOrderId(orderId);
        lockProductStockRequest.setOrderItemRequestList(orderItemRequestList);
        inventoryRemote.deductProductStock(lockProductStockRequest);

        log.info(LoggerFormat.build()
                .remark("CreateOrderDeductStockNode.processInternal-> after deduct stock")
                .finish());
    }

    /**
     * 一致性框架只能拦截public方法
     */
    @ConsistencyTask(id = "rollbackDeductStock", alertActionBeanName = "tendConsistencyAlerter")
    public void doRollback(ReleaseProductStockRequest releaseProductStockRequest) {
        inventoryRemote.releaseProductStock(releaseProductStockRequest);
    }

}
