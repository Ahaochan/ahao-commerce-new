package com.ruyuan.eshop.order.statemachine.action.order.create;

import com.ruyuan.eshop.common.enums.OrderStatusChangeEnum;
import com.ruyuan.eshop.common.enums.OrderStatusEnum;
import com.ruyuan.eshop.order.builder.FullOrderData;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.request.CreateOrderRequest;
import com.ruyuan.eshop.order.domain.request.SubOrderCreateRequest;
import com.ruyuan.eshop.order.statemachine.StateMachineFactory;
import com.ruyuan.eshop.order.statemachine.action.OrderStateAction;
import com.ruyuan.process.engine.model.ProcessContextFactory;
import com.ruyuan.process.engine.process.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 创建订单Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@Component
public class OrderCreateAction extends OrderStateAction<CreateOrderRequest> {

    @Autowired
    private ProcessContextFactory processContextFactory;

    @Autowired
    private StateMachineFactory stateMachineFactory;

    @Override
    public OrderStatusChangeEnum event() {
        return OrderStatusChangeEnum.ORDER_CREATED;
    }

    // 通过状态机context传递，fire事件触发的时候，传递进去context（生单请求）
    @Override
    protected OrderInfoDTO onStateChangeInternal(OrderStatusChangeEnum event, CreateOrderRequest context) {
        // 获取流程引擎并执行
        // 又会在这里把这个request放到业务流程编排引擎context里去
        ProcessContext masterOrderCreateProcess = processContextFactory.getContext("masterOrderCreateProcess");
        masterOrderCreateProcess.set("createOrderRequest", context);
        masterOrderCreateProcess.start();

        // 流程执行完之后，获取返回参数
        Set<Integer> productTypeSet = masterOrderCreateProcess.get("productTypeSet");
        FullOrderData fullOrderData = masterOrderCreateProcess.get("fullMasterOrderData");
        OrderInfoDTO orderInfoDTO = masterOrderCreateProcess.get("orderInfoDTO");
        orderInfoDTO.setProductTypeSet(productTypeSet);
        orderInfoDTO.setFullOrderData(fullOrderData);
        return orderInfoDTO;
    }

    @Override
    protected void postStateChange(OrderStatusChangeEnum event, OrderInfoDTO context) {
        // 先发送主单的状态变更消息
        super.postStateChange(event, context);

        Set<Integer> productTypeSet = context.getProductTypeSet();
        FullOrderData fullOrderData = context.getFullOrderData();
        if (productTypeSet.size() <= 1) {
            return;
        }

        // 存在多种商品类型，需要按商品类型进行拆单
        for (Integer productType : productTypeSet) {
            // 通过状态机来生成子订单
            StateMachineFactory.OrderStateMachine orderStateMachine = stateMachineFactory.getOrderStateMachine(OrderStatusEnum.NULL);
            orderStateMachine.fire(OrderStatusChangeEnum.SUB_ORDER_CREATED,
                    new SubOrderCreateRequest(fullOrderData, productType));
        }
    }

    // 我们一定要去避免和处理这种问题，对每一种商品类型的拆单，触发这个流程之前，是否把这个流程，包裹再我们最终一致性的框架里去
    // 对拆单子流程触发，包裹在最终一致性框架里，在拆单子流程运行之前，先去写一条最终一致性框架的持久化任务，写到db里去，触发拆单子流程，子流程运行失败了以后
    // 我们的最终一致性框架，就可以确保说会不断的去进行重试，直到拆单子流程可以运行成功为止

}
