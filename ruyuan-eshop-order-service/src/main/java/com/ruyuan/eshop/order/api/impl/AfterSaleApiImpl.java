package com.ruyuan.eshop.order.api.impl;

import com.alibaba.fastjson.JSONObject;
import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.enums.AfterSaleStateMachineChangeEnum;
import com.ruyuan.eshop.common.enums.AfterSaleStatusEnum;
import com.ruyuan.eshop.common.enums.CustomerAuditResult;
import com.ruyuan.eshop.common.redis.RedisLock;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.customer.domain.request.CustomerReceiveAfterSaleRequest;
import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.order.api.AfterSaleApi;
import com.ruyuan.eshop.order.dao.AfterSaleRefundDAO;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.dto.CheckLackDTO;
import com.ruyuan.eshop.order.domain.dto.LackDTO;
import com.ruyuan.eshop.order.domain.entity.AfterSaleRefundDO;
import com.ruyuan.eshop.order.domain.request.LackRequest;
import com.ruyuan.eshop.order.domain.request.RefundCallbackRequest;
import com.ruyuan.eshop.order.domain.request.RevokeAfterSaleRequest;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.service.OrderAfterSaleService;
import com.ruyuan.eshop.order.service.OrderLackService;
import com.ruyuan.eshop.order.statemachine.StateMachineFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 订单中心-逆向售后业务接口
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Slf4j
@DubboService(version = "1.0.0", interfaceClass = AfterSaleApi.class, retries = 0)
public class AfterSaleApiImpl implements AfterSaleApi {

    @Autowired
    private OrderLackService orderLackService;

    @Autowired
    private OrderAfterSaleService orderAfterSaleService;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private AfterSaleRefundDAO afterSaleRefundDAO;

    @Autowired
    private StateMachineFactory stateMachineFactory;

    @Override
    public JsonResult<LackDTO> lackItem(LackRequest request) {
        log.info("request={}", JSONObject.toJSONString(request));
        try {
            //1、参数基本校验
            ParamCheckUtil.checkStringNonEmpty(request.getOrderId(), OrderErrorCodeEnum.ORDER_ID_IS_NULL);
            ParamCheckUtil.checkCollectionNonEmpty(request.getLackItems(), OrderErrorCodeEnum.LACK_ITEM_IS_NULL);

            //2、加锁防并发
            String lockKey = RedisLockKeyConstants.LACK_REQUEST_KEY + request.getOrderId();
            boolean isLocked = redisLock.tryLock(lockKey);
            if (!isLocked) {
                throw new OrderBizException(OrderErrorCodeEnum.ORDER_NOT_ALLOW_TO_LACK);
            }
            //3、参数校验
            CheckLackDTO checkResult = orderLackService.checkRequest(request);
            try {
                //4、缺品处理
                return JsonResult.buildSuccess(orderLackService.executeLackRequest(request, checkResult));
            } finally {
                redisLock.unlock(lockKey);
            }
        } catch (OrderBizException e) {
            log.error("biz error", e);
            return JsonResult.buildError(e.getErrorCode(), e.getErrorMsg());
        } catch (Exception e) {
            log.error("error", e);
            return JsonResult.buildError(e.getMessage());
        }
    }

    @Override
    public JsonResult<Boolean> refundCallback(RefundCallbackRequest payRefundCallbackRequest) {
        String orderId = payRefundCallbackRequest.getOrderId();
        log.info("接收到取消订单支付退款回调,orderId:{}", orderId);
        return orderAfterSaleService.receivePaymentRefundCallback(payRefundCallbackRequest);
    }

    @Override
    public JsonResult<Boolean> receiveCustomerAuditResult(CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest) {
        AfterSaleStateMachineDTO afterSaleStateMachineDTO = new AfterSaleStateMachineDTO();
        afterSaleStateMachineDTO.setCustomerReviewReturnGoodsRequest(customerReviewReturnGoodsRequest);

        //  审核通过
        if (CustomerAuditResult.ACCEPT.getCode().equals(customerReviewReturnGoodsRequest.getAuditResult())) {
            //  售后状态机 操作 审核通过更新售后信息 AfterSaleAuditAction
            StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.COMMITTED);
            afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.AUDIT_PASS, afterSaleStateMachineDTO);
        }

        //  审核拒绝
        if (CustomerAuditResult.REJECT.getCode().equals(customerReviewReturnGoodsRequest.getAuditResult())) {
            //  售后状态机 操作 审核拒绝更新售后信息 AfterSaleAuditAction
            StateMachineFactory.AfterSaleStateMachine afterSaleStateMachine = stateMachineFactory.getAfterSaleStateMachine(AfterSaleStatusEnum.COMMITTED);
            afterSaleStateMachine.fire(AfterSaleStateMachineChangeEnum.AUDIT_REJECT, afterSaleStateMachineDTO);
        }

        return JsonResult.buildSuccess(true);
    }

    @Override
    public JsonResult<Boolean> revokeAfterSale(RevokeAfterSaleRequest revokeAfterSaleRequest) {
        orderAfterSaleService.revokeAfterSale(revokeAfterSaleRequest);

        return JsonResult.buildSuccess(true);
    }

    @Override
    public JsonResult<Long> customerFindAfterSaleRefundInfo(CustomerReceiveAfterSaleRequest customerReceiveAfterSaleRequest) {
        String afterSaleId = customerReceiveAfterSaleRequest.getAfterSaleId();
        AfterSaleRefundDO afterSaleRefundDO = afterSaleRefundDAO.findAfterSaleRefundByfterSaleId(afterSaleId);
        if (afterSaleRefundDO == null) {
            throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_REFUND_ID_IS_NULL);
        }
        JsonResult<Long> jsonResult = new JsonResult<>();
        jsonResult.setData(afterSaleRefundDO.getId());
        jsonResult.setSuccess(true);
        return jsonResult;
    }

}