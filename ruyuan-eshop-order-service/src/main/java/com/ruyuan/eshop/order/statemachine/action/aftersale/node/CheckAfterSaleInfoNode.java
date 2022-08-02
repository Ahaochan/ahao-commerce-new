package com.ruyuan.eshop.order.statemachine.action.aftersale.node;

import com.ruyuan.eshop.common.enums.AfterSaleItemTypeEnum;
import com.ruyuan.eshop.common.enums.AfterSaleStatusEnum;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.order.dao.AfterSaleInfoDAO;
import com.ruyuan.eshop.order.dao.AfterSaleItemDAO;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleInfoDO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleItemDO;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.process.engine.process.ProcessContext;
import com.ruyuan.process.engine.process.StandardProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 售后审核 检查请求参数 节点
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class CheckAfterSaleInfoNode extends StandardProcessor {

    @Autowired
    private AfterSaleInfoDAO afterSaleInfoDAO;

    @Autowired
    private AfterSaleItemDAO afterSaleItemDAO;

    @Override
    protected void processInternal(ProcessContext processContext) {

        AfterSaleStateMachineDTO afterSaleStateMachineDTO = processContext.get("afterSaleStateMachineDTO");
        CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest = afterSaleStateMachineDTO.getCustomerReviewReturnGoodsRequest();

        String afterSaleId = customerReviewReturnGoodsRequest.getAfterSaleId();
        String orderId = customerReviewReturnGoodsRequest.getOrderId();
        String skuCode = customerReviewReturnGoodsRequest.getSkuCode();
        Integer itemType = AfterSaleItemTypeEnum.AFTER_SALE_ORDER_ITEM.getCode();

        AfterSaleInfoDO afterSaleInfoDO = afterSaleInfoDAO.getOneByAfterSaleId(customerReviewReturnGoodsRequest.getAfterSaleId());
        AfterSaleItemDO afterSaleItemDO = afterSaleItemDAO.getAfterSaleOrderItem(orderId, afterSaleId, skuCode, itemType);

        //  优惠券类型的售后单因为不用退款，无需审核
        if (AfterSaleItemTypeEnum.AFTER_SALE_COUPON.getCode().equals(customerReviewReturnGoodsRequest.getAfterSaleItemType())) {
            throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_TYPE_IS_ERROR);
        }

        //  大于"提交申请"状态的售后单不能重复发起审核
        if (afterSaleInfoDO.getAfterSaleStatus() > AfterSaleStatusEnum.COMMITTED.getCode()) {
            throw new OrderBizException(OrderErrorCodeEnum.CUSTOMER_AUDIT_CANNOT_REPEAT);
        }

        //  查询 售后订单条目
        ParamCheckUtil.checkObjectNonNull(afterSaleItemDO, OrderErrorCodeEnum.AFTER_SALE_ITEM_CANNOT_NULL);

        processContext.set("afterSaleInfoDO", afterSaleInfoDO);
        processContext.set("afterSaleItemDO", afterSaleItemDO);

    }
}
