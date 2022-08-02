package com.ruyuan.eshop.order.statemachine.action.aftersale;

import com.google.common.collect.Lists;
import com.ruyuan.eshop.common.constants.RedisLockKeyConstants;
import com.ruyuan.eshop.common.core.JsonResult;
import com.ruyuan.eshop.common.enums.*;
import com.ruyuan.eshop.common.exception.BaseBizException;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.market.domain.dto.UserCouponDTO;
import com.ruyuan.eshop.market.domain.query.UserCouponQuery;
import com.ruyuan.eshop.market.enums.CouponUsedStatusEnum;
import com.ruyuan.eshop.order.domain.dto.AfterSaleItemDTO;
import com.ruyuan.eshop.order.domain.dto.AfterSaleStateMachineDTO;
import com.ruyuan.eshop.order.domain.dto.OrderInfoDTO;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.domain.request.ManualAfterSaleDTO;
import com.ruyuan.eshop.order.domain.request.ReturnGoodsOrderRequest;
import com.ruyuan.eshop.order.enums.OrderNoTypeEnum;
import com.ruyuan.eshop.order.enums.ReturnGoodsTypeEnum;
import com.ruyuan.eshop.order.exception.OrderBizException;
import com.ruyuan.eshop.order.exception.OrderErrorCodeEnum;
import com.ruyuan.eshop.order.service.RocketMqService;
import com.ruyuan.eshop.order.service.impl.OrderPaymentDetailFactory;
import com.ruyuan.eshop.order.statemachine.action.AfterSaleStateAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 创建售后信息Action
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
@Slf4j
public class AfterSaleCreatedInfoAction extends AfterSaleStateAction<AfterSaleStateMachineDTO> {

    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private RocketMqService rocketMqService;

    @Autowired
    private OrderPaymentDetailFactory orderPaymentDetailFactory;

    /**
     * 用于生成优惠券和运费的售后单的退货数量,默认退优惠券和退运费的数量都是1
     */
    private static final Integer AFTER_SALE_RETURN_QUANTITY = 1;

    @Override
    public AfterSaleStateMachineChangeEnum event() {
        return AfterSaleStateMachineChangeEnum.INITIATE_AFTER_SALE;
    }

    /**
     * 业务更新说明：更新售后业务,将尾笔订单条目的验证标准细化到sku数量维度
     * <p>
     * 售后单生成规则说明：<br>
     * 1.当前售后条目非尾笔,只生成一笔订单条目的售后单<br>
     * 2.当前售后条目尾笔,生成3笔售后单,分别是:订单条目售后单、优惠券售后单、运费售后单
     * <p>
     * 业务场景说明：<br>
     * 场景1：订单有2笔条目,A条目商品总数量:10,B条目商品总数量:1<br>
     * 第一次：A发起售后,售后数量1,已退数量1<br>
     * 第二次：A发起售后,售后数量2,已退数量3<br>
     * 第三次：A发起售后,售后数量7,已退数量10,A条目全部退完<br>
     * 第四次：B发起售后,售后数量1,已退数量1,本次售后条目是当前订单的最后一条,补退优惠券和运费<br>
     * <p>
     * 场景2：订单有1笔条目,条目商品总数量和申请售后数量相同,直接全部退掉,补退优惠券和运费
     * <p>
     * 场景3：订单有1笔条目,条目商品总数量2<br>
     * 第一次：条目申请售后,售后数量1<br>
     * 第二次：条目申请售后,售后数量1,本次售后条目是当前订单的最后一条,补退优惠券和运费
     */
    @Override
    protected AfterSaleStateMachineDTO onStateChangeInternal(AfterSaleStateMachineChangeEnum event, AfterSaleStateMachineDTO afterSaleStateMachineDTO) {
        //  @Transactional无法生效，需要用编程式事务
        return transactionTemplate.execute(transactionStatus -> {
            ReturnGoodsOrderRequest returnGoodsOrderRequest = afterSaleStateMachineDTO.getReturnGoodsOrderRequest();

            //  1、请求参数校验
            checkAfterSaleRequestParam(returnGoodsOrderRequest);

            //  2、分布式锁
            String key = RedisLockKeyConstants.REFUND_KEY + returnGoodsOrderRequest.getOrderId();
            boolean lock = redisLock.tryLock(key);
            if (!lock) {
                throw new OrderBizException(OrderErrorCodeEnum.PROCESS_AFTER_SALE_RETURN_GOODS);
            }

            try {
                //  3、封装手动售后数据
                ManualAfterSaleDTO manualAfterSaleDTO = buildOrderInfo(returnGoodsOrderRequest);

                //  4、方法核心逻辑：判断本次售后是否是当前订单的尾笔条目   (尾笔:就是最后一条)
                manualAfterSaleDTO = checkLastOrderItem(manualAfterSaleDTO);

                //  5、生成售后单号
                generateAfterSaleId(manualAfterSaleDTO);

                //  6、新增售后信息
                insertAfterSaleInfo(manualAfterSaleDTO, event);

                //  7、发送实际退款MQ
                sendAfterSaleRefundMq(manualAfterSaleDTO);

            } catch (BaseBizException e) {
                log.error("biz error", e);
                throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_FAILED);
            } finally {
                redisLock.unlock(key);
            }
            return afterSaleStateMachineDTO;
        });
    }

    private void sendAfterSaleRefundMq(ManualAfterSaleDTO manualAfterSaleDTO) {

        rocketMqService.sendAfterSaleRefundMessage(manualAfterSaleDTO);
    }

    private void generateAfterSaleId(ManualAfterSaleDTO manualAfterSaleDTO) {
        OrderInfoDTO orderInfoDTO = manualAfterSaleDTO.getOrderInfoDTO();
        OrderInfoDO orderInfoDO = orderConverter.orderInfoDTO2DO(orderInfoDTO);
        String afterSaleId = orderNoManager.genOrderId(OrderNoTypeEnum.AFTER_SALE.getCode(), orderInfoDO.getUserId());
        manualAfterSaleDTO.setAfterSaleId(afterSaleId);
    }

    private void insertAfterSaleInfo(ManualAfterSaleDTO manualAfterSaleDTO, AfterSaleStateMachineChangeEnum event) {
        transactionTemplate.execute(transactionStatus -> {
            //  1、新增售后订单表
            AfterSaleInfoDO afterSaleInfoDO = insertAfterSaleOrderInfo(manualAfterSaleDTO);

            //  2、新增售后条目表
            insertAfterSaleOrderItem(manualAfterSaleDTO);

            //  3、新增售后变更表
            insertAfterSaleLog(manualAfterSaleDTO, event);

            //  4、新增售后支付表
            insertAfterSaleRefund(manualAfterSaleDTO, afterSaleInfoDO);
            return true;
        });
    }

    private void checkAfterSaleRequestParam(ReturnGoodsOrderRequest returnGoodsOrderRequest) {
        ParamCheckUtil.checkObjectNonNull(returnGoodsOrderRequest);

        String orderId = returnGoodsOrderRequest.getOrderId();
        ParamCheckUtil.checkStringNonEmpty(orderId, OrderErrorCodeEnum.ORDER_ID_IS_NULL);

        String userId = returnGoodsOrderRequest.getUserId();
        ParamCheckUtil.checkStringNonEmpty(userId, OrderErrorCodeEnum.USER_ID_IS_NULL);

        Integer businessIdentifier = returnGoodsOrderRequest.getBusinessIdentifier();
        ParamCheckUtil.checkObjectNonNull(businessIdentifier, OrderErrorCodeEnum.BUSINESS_IDENTIFIER_IS_NULL);

        Integer returnGoodsCode = returnGoodsOrderRequest.getReturnGoodsCode();
        ParamCheckUtil.checkObjectNonNull(returnGoodsCode, OrderErrorCodeEnum.RETURN_GOODS_CODE_IS_NULL);

        String skuCode = returnGoodsOrderRequest.getSkuCode();
        ParamCheckUtil.checkStringNonEmpty(skuCode, OrderErrorCodeEnum.SKU_IS_NULL);

        Integer returnNum = returnGoodsOrderRequest.getReturnQuantity();
        ParamCheckUtil.checkObjectNonNull(returnNum, OrderErrorCodeEnum.RETURN_GOODS_NUM_IS_NULL);

        OrderInfoDO orderInfo = orderInfoDAO.getByOrderId(orderId);
        ParamCheckUtil.checkObjectNonNull(orderInfo, OrderErrorCodeEnum.ORDER_INFO_IS_NULL);

        //  非已签收订单不能申请售后
        if (!OrderStatusEnum.SIGNED.getCode().equals(orderInfo.getOrderStatus())) {
            throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_ORDER_STATUS_ERROR);
        }

        //  虚拟订单不能售后
        if (OrderTypeEnum.VIRTUAL.getCode().equals(orderInfo.getOrderType())) {
            throw new OrderBizException(OrderErrorCodeEnum.VIRTUAL_ORDER_CANNOT_AFTER_SALE);
        }
    }

    private ManualAfterSaleDTO buildOrderInfo(ReturnGoodsOrderRequest returnGoodsOrderRequest) {
        ManualAfterSaleDTO manualAfterSaleDTO = afterSaleConverter.returnGoodRequest2AssembleRequest(returnGoodsOrderRequest);
        OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(manualAfterSaleDTO.getOrderId());
        OrderInfoDTO orderInfoDTO = orderConverter.orderInfoDO2DTO(orderInfoDO);
        manualAfterSaleDTO.setOrderInfoDTO(orderInfoDTO);
        return manualAfterSaleDTO;
    }

    /**
     * 尾笔条目判断规则：
     * 用当前条目的"商品总数量"和"已售后数量"作比较
     * 1、商品总数量 < 已售后数量 总共才买1个,售后数量却是2个  售后的数据错误
     * 2、商品总数量 > 已售后数量 条目还没有退完,当前不是最后一笔
     * 3、商品总数量 = 已售后数量 本次售后条目退完,但是如果订单有多条目,需要继续验证是否全部条目均已退完
     */
    private ManualAfterSaleDTO checkLastOrderItem(ManualAfterSaleDTO manualAfterSaleDTO) {
        //  预备参数
        String orderId = manualAfterSaleDTO.getOrderId();
        String skuCode = manualAfterSaleDTO.getSkuCode();
        //  订单条目数
        List<OrderItemDO> allOrderItemList = orderItemDAO.listByOrderId(orderId);
        int orderItemTotal = allOrderItemList.size();
        List<AfterSaleItemDO> afterSaleItemDOList = afterSaleItemDAO.getOrderIdAndSkuCode(orderId, skuCode);

        //  售后条目
        OrderItemDO orderItemDO;
        if (orderItemTotal == 1) {
            //  如果只有1条,就是当前售后的这条
            orderItemDO = allOrderItemList.get(0);
        } else {
            //  有如果多条,取出本次要售后的条目
            orderItemDO = orderItemDAO.getOrderIdAndSkuCode(orderId, skuCode);
        }

        //  商品总数量 比较 已售后数量
        switch (compareQuantity(orderItemDO, manualAfterSaleDTO, afterSaleItemDOList)) {
            case -1:
                //  商品总数量 < 已售后数量
                throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_QUANTITY_IS_ERROR);
            case 1:
                //  商品总数量 > 已售后数量
                return processNonLastOrderItems(manualAfterSaleDTO, orderItemDO, afterSaleItemDOList);
            default:
                //  商品总数量 = 已售后数量
                return processLastOrderItems(manualAfterSaleDTO, orderItemDO, orderId, orderItemTotal, afterSaleItemDOList);
        }
    }

    /**
     * 比较数量
     */
    private int compareQuantity(OrderItemDO orderItemDO, ManualAfterSaleDTO manualAfterSaleDTO, List<AfterSaleItemDO> afterSaleItemDOList) {
        //  商品总数量
        Integer orderItemSaleQuantity = orderItemDO.getSaleQuantity();

        //  已售后数量 = 本次请求的售后数量 + 已经售后过的数量
        Integer afterSaleQuantity = calculateOrderItemAfterSaleNum(manualAfterSaleDTO, afterSaleItemDOList);

        //  作比较
        return orderItemSaleQuantity.compareTo(afterSaleQuantity);
    }

    /**
     * 商品总数量 > 已售后数量
     * 非最后一笔条目
     */
    private ManualAfterSaleDTO processNonLastOrderItems(ManualAfterSaleDTO manualAfterSaleDTO, OrderItemDO orderItemDO,
                                                        List<AfterSaleItemDO> afterSaleItemDOList) {
        //  非尾笔订单,只记录1笔售后条目
        return buildAfterSaleItem(manualAfterSaleDTO, orderItemDO, afterSaleItemDOList);
    }

    /**
     * 商品总数量 = 已售后数量
     * <p>
     * 继续验证订单的多条目是否都退完
     * 都退完,当前是最后一笔
     * 没有退完,非最后一笔
     */
    private ManualAfterSaleDTO processLastOrderItems(ManualAfterSaleDTO manualAfterSaleDTO, OrderItemDO orderItemDO,
                                                     String orderId, int orderItemTotal, List<AfterSaleItemDO> afterSaleItemDOList) {

        //  数据库中保存此笔订单已全部退货完毕的条目数
        Integer returnCompletionTotal = getReturnCompletionTotal(orderId);

        /*
            此处+1的业务说明：已全部退货完毕的条目数 >= 订单总条目数
            订单有2笔条目，A条目总数10，B条目总数1
            场景1：
                第一次：A退10
                第二次：B退1
                这里 returnCompletionTotal(退过2次) = orderItemTotal (订单总条数 2条)
            场景2：
                第一次：A退1
                第二次：A退9
                第三次：B退1
                这里 returnCompletionTotal(退过3次) > orderItemTotal (订单总条数 2条)
         */
        if ((returnCompletionTotal + 1) >= orderItemTotal) {
            //  最后一笔
            return buildLastOrderItem(manualAfterSaleDTO, orderItemDO, afterSaleItemDOList);
        }
        //  非最后一笔
        return processNonLastOrderItems(manualAfterSaleDTO, orderItemDO, afterSaleItemDOList);
    }

    private Integer getReturnCompletionTotal(String orderId) {
        List<AfterSaleItemDO> afterSaleItemDOListReturn = afterSaleItemDAO.listReturnCompletionByOrderId(
                orderId, AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode());
        return afterSaleItemDOListReturn.size();
    }

    private ManualAfterSaleDTO buildLastOrderItem(ManualAfterSaleDTO manualAfterSaleDTO, OrderItemDO orderItemDO,
                                                  List<AfterSaleItemDO> afterSaleItemDOList) {
        String orderId = manualAfterSaleDTO.getOrderId();
        OrderInfoDO orderInfoDO = orderInfoDAO.getByOrderId(orderId);

        //  尾笔订单,共记录3笔售后条目、优惠券售后单(如存在)、运费(如存在)
        buildAfterSaleItem(manualAfterSaleDTO, orderItemDO, afterSaleItemDOList);

        //  记录优惠券售后单
        buildAfterSaleCoupon(manualAfterSaleDTO, orderId, orderInfoDO);

        //  记录运费售后单
        buildAfterSaleFreight(manualAfterSaleDTO, orderId, orderInfoDO);

        return manualAfterSaleDTO;
    }

    private void buildAfterSaleFreight(ManualAfterSaleDTO manualAfterSaleDTO, String orderId, OrderInfoDO orderInfoDO) {
        //  查运费
        OrderAmountDO deliveryAmount = orderAmountDAO.getOne(orderId, AmountTypeEnum.SHIPPING_AMOUNT.getCode());
        Integer freightAmount = (deliveryAmount == null || deliveryAmount.getAmount() == null) ? 0 : deliveryAmount.getAmount();
        if (freightAmount.equals(0)) {
            //  没有运费，不用退
            return;
        }

        //  产生运费了,记一条补退运费的售后单
        String afterSaleId = orderNoManager.genOrderId(OrderNoTypeEnum.AFTER_SALE.getCode(), orderInfoDO.getUserId());

        //  创建运费售后单 运费售后单没有商品名称
        AfterSaleItemDTO afterSaleItemDTO = buildAfterSaleItemData(orderId, afterSaleId, manualAfterSaleDTO.getSkuCode(),
                AmountTypeEnum.SHIPPING_AMOUNT.getMsg(), AfterSaleItemTypeEnum.AFTER_SALE_FREIGHT.getCode());

        //  运费金额
        afterSaleItemDTO.setOriginAmount(freightAmount);
        afterSaleItemDTO.setApplyRefundAmount(freightAmount);
        afterSaleItemDTO.setRealRefundAmount(freightAmount);

        manualAfterSaleDTO.getAfterSaleItemDTOList().add(afterSaleItemDTO);
    }

    private AfterSaleItemDTO buildAfterSaleItemData(String orderId, String afterSaleId, String skuCode,
                                                    String productName, Integer afterSaleItemTypeCode) {
        AfterSaleItemDTO afterSaleItemDTO = new AfterSaleItemDTO();
        afterSaleItemDTO.setAfterSaleId(afterSaleId);
        afterSaleItemDTO.setOrderId(orderId);
        afterSaleItemDTO.setProductName(productName);
        afterSaleItemDTO.setAfterSaleItemType(afterSaleItemTypeCode);
        afterSaleItemDTO.setSkuCode(skuCode);
        afterSaleItemDTO.setReturnQuantity(AFTER_SALE_RETURN_QUANTITY);
        afterSaleItemDTO.setReturnCompletionMark(AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode());
        return afterSaleItemDTO;
    }

    private void buildAfterSaleCoupon(ManualAfterSaleDTO manualAfterSaleDTO,
                                      String orderId, OrderInfoDO orderInfoDO) {
        String userId = manualAfterSaleDTO.getUserId();
        String couponId = orderInfoDO.getCouponId();
        //  没有优惠券,不用退
        if (StringUtils.isEmpty(couponId)) {
            return;
        }

        UserCouponQuery userCouponQuery = new UserCouponQuery();
        userCouponQuery.setUserId(userId);
        userCouponQuery.setCouponId(couponId);

        //  查优惠券
        JsonResult<UserCouponDTO> userCoupon = marketApi.getUserCoupon(userCouponQuery);
        if (!userCoupon.getSuccess() || userCoupon.getData() == null) {
            throw new OrderBizException(OrderErrorCodeEnum.AFTER_SALE_COUPON_IS_ERROR);
        }

        UserCouponDTO userCouponDTO = userCoupon.getData();
        Integer used = userCouponDTO.getUsed();
        //  没使用优惠券,不用退
        if (used.equals(CouponUsedStatusEnum.UN_USED.getCode())) {
            return;
        }

        //  订单使用优惠券了,记一条补退优惠券的售后单
        String afterSaleId = orderNoManager.genOrderId(OrderNoTypeEnum.AFTER_SALE.getCode(), orderInfoDO.getUserId());

        //  创建优惠券售后单 优惠券id作为售后单商品名称
        AfterSaleItemDTO afterSaleItemDTO = buildAfterSaleItemData(orderId, afterSaleId, manualAfterSaleDTO.getSkuCode(),
                userCouponDTO.getCouponId(), AfterSaleItemTypeEnum.AFTER_SALE_COUPON.getCode());

        manualAfterSaleDTO.getAfterSaleItemDTOList().add(afterSaleItemDTO);
    }

    /**
     * 非尾笔订单 填充售后条目数据
     */
    private ManualAfterSaleDTO buildAfterSaleItem(ManualAfterSaleDTO manualAfterSaleDTO, OrderItemDO orderItemDO,
                                                  List<AfterSaleItemDO> afterSaleItemDOList) {
        //  填充售后条目数据
        List<AfterSaleItemDTO> afterSaleItemDTOList = buildAfterSaleItemList(manualAfterSaleDTO, orderItemDO, afterSaleItemDOList);
        manualAfterSaleDTO.setAfterSaleItemDTOList(afterSaleItemDTOList);
        manualAfterSaleDTO.setAfterSaleType(AfterSaleTypeEnum.RETURN_GOODS.getCode());

        //  计算金额公式：单价 = 总价 / 销量
        //  应付金额的单价
        int originalUnitPrice = orderItemDO.getOriginAmount() / orderItemDO.getSaleQuantity();
        //  实付金额的单价
        int payUnitPrice = orderItemDO.getPayAmount() / orderItemDO.getSaleQuantity();

        manualAfterSaleDTO.setOriginAmount(originalUnitPrice * manualAfterSaleDTO.getReturnQuantity());
        manualAfterSaleDTO.setApplyRefundAmount(originalUnitPrice * manualAfterSaleDTO.getReturnQuantity());
        manualAfterSaleDTO.setReturnGoodAmount(payUnitPrice * manualAfterSaleDTO.getReturnQuantity());

        return manualAfterSaleDTO;
    }

    private Integer calculateOrderItemAfterSaleNum(ManualAfterSaleDTO manualAfterSaleDTO, List<AfterSaleItemDO> afterSaleItemDOList) {
        //  本次请求的售后数量
        Integer currentRequestAfterSaleQuantity = manualAfterSaleDTO.getReturnQuantity();

        //  数据库记录中已售后过的数量,条目如果是首次售后 afterSaleItemDOList是0
        Integer alreadyReturnQuantity = afterSaleItemDOList.size() == 0
                ? 0
                : afterSaleItemDOList.stream().mapToInt(AfterSaleItemDO::getReturnQuantity).sum();

        //  二者相加的最终结果 是这笔条目的最终售后数量
        return currentRequestAfterSaleQuantity + alreadyReturnQuantity;
    }

    private List<AfterSaleItemDTO> buildAfterSaleItemList(ManualAfterSaleDTO manualAfterSaleDTO, OrderItemDO orderItemDO,
                                                          List<AfterSaleItemDO> afterSaleItemDOList) {
        AfterSaleItemDTO afterSaleItemDTO = afterSaleConverter.orderItemDO2AfterSaleItemDTO(orderItemDO);
        afterSaleItemDTO.setAfterSaleItemType(AfterSaleItemTypeEnum.AFTER_SALE_ORDER_ITEM.getCode());
        afterSaleItemDTO.setApplyRefundAmount(orderItemDO.getOriginAmount());
        afterSaleItemDTO.setRealRefundAmount(orderItemDO.getPayAmount());
        afterSaleItemDTO.setReturnQuantity(manualAfterSaleDTO.getReturnQuantity());
        //  本次请求的售后数量
        Integer requestReturnQuantity = manualAfterSaleDTO.getReturnQuantity();
        Integer saleQuantity = orderItemDO.getSaleQuantity();
        /*
           退货完成标记ReturnCompletionMark的说明：初始默认是10,此条目全部退完更新为20
           例如：当前条目一共有10个sku
           第一次退1个,mark是10
           第二次退2个,mark是10
           第三次退7个,条目退完,mark是20
         */
        //  初始默认是10
        afterSaleItemDTO.setReturnCompletionMark(AfterSaleReturnCompletionMarkEnum.NOT_ALL_RETURN_GOODS.getCode());

        //  查售后记录是空 此条目还没售后过
        //  此条目的商品总数量 == 本次请求售后条目数  这条全退
        if (afterSaleItemDOList.size() == 0 && saleQuantity.equals(requestReturnQuantity)) {
            afterSaleItemDTO.setReturnCompletionMark(AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode());
        }
        //  售后过
        //  此条目的商品总数量 == 已经售后过的数 + 本次售后条目数 这条全退
        int returnQuantitySum = afterSaleItemDOList.stream().mapToInt(AfterSaleItemDO::getReturnQuantity).sum();
        if (afterSaleItemDOList.size() != 0 && saleQuantity.equals(returnQuantitySum + requestReturnQuantity)) {
            //  mark更新为20
            updateAfterSaleItemCompletionMark(orderItemDO.getOrderId());
            afterSaleItemDTO.setReturnCompletionMark(AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode());
        }
        List<AfterSaleItemDTO> afterSaleItemDTOList = new ArrayList<>();
        afterSaleItemDTOList.add(afterSaleItemDTO);
        return afterSaleItemDTOList;
    }

    private void updateAfterSaleItemCompletionMark(String orderId) {
        afterSaleItemDAO.updateAfterSaleItemCompletionMark(
                orderId, AfterSaleReturnCompletionMarkEnum.ALL_RETURN_GOODS.getCode());
    }

    /**
     * 售后退货流程 插入订单销售表
     */
    private AfterSaleInfoDO insertAfterSaleOrderInfo(ManualAfterSaleDTO manualAfterSaleDTO) {
        String orderId = manualAfterSaleDTO.getOrderId();
        String afterSaleId = manualAfterSaleDTO.getAfterSaleId();

        Integer afterSaleType = manualAfterSaleDTO.getAfterSaleType();
        AfterSaleInfoDO afterSaleInfoDO = new AfterSaleInfoDO();
        //  售后退货过程中的 申请退款金额 和 实际退款金额 是计算出来的,金额有可能不同
        afterSaleInfoDO.setAfterSaleId(afterSaleId);
        afterSaleInfoDO.setBusinessIdentifier(BusinessIdentifierEnum.SELF_MALL.getCode());
        afterSaleInfoDO.setOrderId(orderId);
        afterSaleInfoDO.setOrderSourceChannel(BusinessIdentifierEnum.SELF_MALL.getCode());
        afterSaleInfoDO.setUserId(manualAfterSaleDTO.getUserId());
        afterSaleInfoDO.setOrderType(OrderTypeEnum.NORMAL.getCode());
        afterSaleInfoDO.setApplyTime(new Date());
        afterSaleInfoDO.setAfterSaleStatus(event().getToStatus().getCode());
        //  用户售后退货的业务值
        afterSaleInfoDO.setApplySource(AfterSaleApplySourceEnum.USER_RETURN_GOODS.getCode());
        afterSaleInfoDO.setRemark(ReturnGoodsTypeEnum.AFTER_SALE_RETURN_GOODS.getMsg());
        afterSaleInfoDO.setApplyReasonCode(AfterSaleReasonEnum.USER.getCode());
        afterSaleInfoDO.setApplyReason(AfterSaleReasonEnum.USER.getMsg());
        afterSaleInfoDO.setAfterSaleTypeDetail(AfterSaleTypeDetailEnum.PART_REFUND.getCode());
        afterSaleInfoDO.setApplyRefundAmount(manualAfterSaleDTO.getApplyRefundAmount());
        afterSaleInfoDO.setRealRefundAmount(manualAfterSaleDTO.getReturnGoodAmount());
        //  售后退货 只退订单的一笔条目
        if (AfterSaleTypeEnum.RETURN_GOODS.getCode().equals(afterSaleType)) {
            afterSaleInfoDO.setAfterSaleType(AfterSaleTypeEnum.RETURN_GOODS.getCode());
        }
        //  整笔退款 退订单的全部条目 后续按照整笔退款逻辑处理
        if (AfterSaleTypeEnum.RETURN_MONEY.getCode().equals(afterSaleType)) {
            afterSaleInfoDO.setAfterSaleType(AfterSaleTypeEnum.RETURN_MONEY.getCode());
        }

        afterSaleInfoDAO.save(afterSaleInfoDO);
        log.info("新增订单售后记录,订单号:{},售后单号:{},订单售后状态:{}", orderId, afterSaleId,
                afterSaleInfoDO.getAfterSaleStatus());
        return afterSaleInfoDO;
    }

    private void insertAfterSaleOrderItem(ManualAfterSaleDTO manualAfterSaleDTO) {
        List<AfterSaleItemDO> afterSaleItemDOList = Lists.newArrayList();
        List<AfterSaleItemDTO> afterSaleItemDTOList = manualAfterSaleDTO.getAfterSaleItemDTOList();
        String orderId = manualAfterSaleDTO.getOrderId();
        String afterSaleId = manualAfterSaleDTO.getAfterSaleId();

        for (AfterSaleItemDTO afterSaleItemDTO : afterSaleItemDTOList) {
            AfterSaleItemDO afterSaleItemDO = orderConverter.convertAfterSaleItemDO(afterSaleItemDTO);
            afterSaleItemDO.setAfterSaleId(afterSaleId);
            afterSaleItemDO.setOrderId(orderId);
            //  条目售后单
            if (AfterSaleItemTypeEnum.AFTER_SALE_ORDER_ITEM.getCode().equals(afterSaleItemDTO.getAfterSaleItemType())) {
                //  保存的是计算后的原价和实付退款金额
                afterSaleItemDO.setOriginAmount(manualAfterSaleDTO.getOriginAmount());
                afterSaleItemDO.setApplyRefundAmount(manualAfterSaleDTO.getApplyRefundAmount());
                afterSaleItemDO.setRealRefundAmount(manualAfterSaleDTO.getReturnGoodAmount());
            }
            //  运费售后单
            if (AfterSaleItemTypeEnum.AFTER_SALE_FREIGHT.getCode().equals(afterSaleItemDTO.getAfterSaleItemType())) {
                //  保存的是运费
                afterSaleItemDO.setOriginAmount(afterSaleItemDTO.getOriginAmount());
                afterSaleItemDO.setApplyRefundAmount(afterSaleItemDTO.getApplyRefundAmount());
                afterSaleItemDO.setRealRefundAmount(afterSaleItemDTO.getRealRefundAmount());
            }
            //  说明：优惠券售后单因为只退券不退钱,所以不保存金额
            afterSaleItemDOList.add(afterSaleItemDO);
        }
        afterSaleItemDAO.saveBatch(afterSaleItemDOList);
        log.info("新增订单售后条目,订单号:{}", orderId);
    }

    private void insertAfterSaleLog(ManualAfterSaleDTO manualAfterSaleDTO, AfterSaleStateMachineChangeEnum event) {
        String orderId = manualAfterSaleDTO.getOrderId();
        String afterSaleId = manualAfterSaleDTO.getAfterSaleId();

        AfterSaleLogDO afterSaleLogDO = new AfterSaleLogDO();
        afterSaleLogDO.setAfterSaleId(afterSaleId);
        afterSaleLogDO.setPreStatus(event.getFromStatus().getCode());
        afterSaleLogDO.setCurrentStatus(event.getToStatus().getCode());
        afterSaleLogDO.setOrderId(orderId);
        afterSaleLogDO.setRemark(ReturnGoodsTypeEnum.AFTER_SALE_RETURN_GOODS.getMsg());

        afterSaleLogDAO.save(afterSaleLogDO);
        log.info("新增售后单变更信息, 售后单号:{},状态:PreStatus{},CurrentStatus:{}", afterSaleLogDO.getAfterSaleId(),
                afterSaleLogDO.getPreStatus(), afterSaleLogDO.getCurrentStatus());
    }

    private void insertAfterSaleRefund(ManualAfterSaleDTO manualAfterSaleDTO, AfterSaleInfoDO afterSaleInfoDO) {
        String orderId = manualAfterSaleDTO.getOrderId();
        String afterSaleId = manualAfterSaleDTO.getAfterSaleId();
        String skuCode = manualAfterSaleDTO.getSkuCode();

        AfterSaleRefundDO afterSaleRefundDO = orderPaymentDetailFactory.get(orderId, afterSaleId);
        //  最后一笔的售后条目,实际退款金额 = 条目退款金额 + 运费
        AfterSaleItemDO afterSaleOrderItem = afterSaleItemDAO.getAfterSaleOrderItem(orderId, afterSaleId,
                skuCode, AfterSaleItemTypeEnum.AFTER_SALE_FREIGHT.getCode());

        if (afterSaleOrderItem != null) {
            afterSaleRefundDO.setRefundAmount(afterSaleInfoDO.getRealRefundAmount() + afterSaleOrderItem.getRealRefundAmount());
        } else {
            afterSaleRefundDO.setRefundAmount(afterSaleInfoDO.getRealRefundAmount());
        }

        afterSaleRefundDAO.save(afterSaleRefundDO);
        log.info("新增售后支付信息,订单号:{},售后单号:{},状态:{}", orderId, afterSaleId, afterSaleRefundDO.getRefundStatus());
    }
}
