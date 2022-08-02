package com.ruyuan.eshop.order.statemachine.action;

import com.ruyuan.eshop.common.enums.AfterSaleStateMachineChangeEnum;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.market.api.MarketApi;
import com.ruyuan.eshop.order.converter.AfterSaleConverter;
import com.ruyuan.eshop.order.converter.OrderConverter;
import com.ruyuan.eshop.order.dao.*;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.manager.OrderNoManager;
import com.ruyuan.eshop.order.remote.PayRemote;
import com.ruyuan.eshop.order.service.OrderAfterSaleService;
import com.ruyuan.eshop.order.service.impl.AfterSaleOperateLogFactory;
import com.ruyuan.eshop.order.statemachine.StateMachineFactory;
import com.ruyuan.process.engine.model.ProcessContextFactory;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 售后状态action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
public abstract class AfterSaleStateAction<T> extends AbstractStateAction<T, AfterSaleStateMachineDTO, AfterSaleStateMachineChangeEnum> {

    @Autowired
    public OrderConverter orderConverter;

    @Autowired
    public AfterSaleLogDAO afterSaleLogDAO;

    @Autowired
    public AfterSaleInfoDAO afterSaleInfoDAO;

    @Autowired
    public AfterSaleItemDAO afterSaleItemDAO;

    @Autowired
    public OrderPaymentDetailDAO orderPaymentDetailDAO;

    @Autowired
    public AfterSaleRefundDAO afterSaleRefundDAO;

    @Autowired
    public AfterSaleOperateLogFactory afterSaleOperateLogFactory;

    @Autowired
    public AfterSaleConverter afterSaleConverter;

    @Autowired
    public OrderInfoDAO orderInfoDAO;

    @Autowired
    public RedisLock redisLock;

    @Autowired
    public StateMachineFactory stateMachineFactory;

    @Autowired
    public OrderAfterSaleService orderAfterSaleService;

    @Autowired
    public OrderNoManager orderNoManager;

    @DubboReference(version = "1.0.0")
    public MarketApi marketApi;

    @Autowired
    public OrderAmountDAO orderAmountDAO;

    @Autowired
    public OrderItemDAO orderItemDAO;

    @Autowired
    public PayRemote payRemote;

    @Autowired(required = false)
    public ProcessContextFactory processContextFactory;
}
