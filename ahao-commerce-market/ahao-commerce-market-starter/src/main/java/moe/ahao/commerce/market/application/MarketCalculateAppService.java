package moe.ahao.commerce.market.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.AmountTypeEnum;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.commerce.market.infrastructure.exception.MarketExceptionEnum;
import moe.ahao.commerce.market.infrastructure.properties.MarketProperties;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.CouponDO;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.data.FreightTemplateDO;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper.CouponMapper;
import moe.ahao.commerce.market.infrastructure.repository.impl.mybatis.mapper.FreightTemplateMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 营销管理service组件
 */
@Slf4j
@Service
public class MarketCalculateAppService {
    @Autowired
    private MarketProperties marketProperties;

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private FreightTemplateMapper freightTemplateMapper;

    /**
     * 计算订单费用
     * 假设订单有两个商品条目记录，分摊优惠券的规则如下：
     * 商品1
     * 单价（单位分）    购买数量    小计
     * 1000            10         1000 * 10
     * 商品2
     * 单价    购买数量    小计
     * 100    1         100 * 1
     * 整单优惠券抵扣5元，也就是500分
     * <p>
     * 则商品1分摊的优惠券抵扣金额为：
     * 优惠券抵扣总额 * (商品1单价*商品1购买数量)/((商品1单价*商品1购买数量) + (商品2单价*商品2购买数量))
     * = 500 * (1000 * 10) / ((1000 * 10)  + (100 * 1) )
     * = 5000000 / 10100
     * = 495 分
     * 同样的逻辑可计算出商品2分摊的优惠券抵扣金额为5分，也就是0.05元
     * <p>
     * 如果计算出的优惠券分摊到一条 item 上存在小数时，则向上取整
     * 然后最后一条 item 分摊的金额就用优惠金额减掉前面所有优惠的item分摊的总额
     */
    public CalculateOrderAmountDTO calculateOrderAmount(CalculateOrderAmountQuery query) {
        // 1. 检查入参
        this.check(query);

        String orderId = query.getOrderId();
        String userId = query.getUserId();
        String couponId = query.getCouponId();
        String regionId = query.getRegionId();
        List<CalculateOrderAmountQuery.OrderItem> orderItemList = query.getOrderItemList();

        // 2. 获取优惠券抵扣金额
        BigDecimal discountAmount = this.getCouponDiscountAmount(orderId, userId, couponId);

        // 3. 获取订单总金额
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        for (CalculateOrderAmountQuery.OrderItem orderItem : orderItemList) {
            BigDecimal salePrice = orderItem.getSalePrice();
            BigDecimal saleQuantity = orderItem.getSaleQuantity();
            BigDecimal orderItemAmount = salePrice.multiply(saleQuantity);
            totalOrderAmount = totalOrderAmount.add(orderItemAmount);
        }

        // 4. 计算每个商品条目的分摊金额, 重新求和
        BigDecimal reTotalOrderAmount = BigDecimal.ZERO;
        BigDecimal reTotalDiscountAmount = BigDecimal.ZERO;
        BigDecimal reTalRealPayAmount = BigDecimal.ZERO;
        BigDecimal notLastTotalDiscountAmount = BigDecimal.ZERO;
        List<CalculateOrderAmountDTO.OrderItemAmountDTO> orderAmountDetailList = new ArrayList<>();
        for (int index = 0, totalNum = orderItemList.size(); index < totalNum; index++) {
            CalculateOrderAmountQuery.OrderItem orderItem = orderItemList.get(index);
            // 4.1. 计算每个订单条目的支付原价
            BigDecimal salePrice = orderItem.getSalePrice();
            BigDecimal saleQuantity = orderItem.getSaleQuantity();
            BigDecimal orderItemAmount = salePrice.multiply(saleQuantity);

            // 4.2. 计算每个订单条目的优惠金额
            BigDecimal orderItemDiscountAmount = BigDecimal.ZERO;
            if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                if (index < totalNum - 1) {
                    // 订单条目分摊的优惠金额
                    BigDecimal discountRate = orderItemAmount.divide(totalOrderAmount, 2, RoundingMode.DOWN);
                    orderItemDiscountAmount = discountAmount.multiply(discountRate);
                    notLastTotalDiscountAmount = notLastTotalDiscountAmount.add(orderItemDiscountAmount);
                } else {
                    // 最后一条item的优惠金额 = 总优惠金额 - 前面所有item分摊的优惠总额
                    orderItemDiscountAmount = discountAmount.subtract(notLastTotalDiscountAmount);
                }
            }

            // 4.3. 计算每个条目的实付金额
            BigDecimal orderItemPayAmount = orderItemAmount.subtract(orderItemDiscountAmount);

            // 4.4. 封装响应体
            CalculateOrderAmountDTO.OrderItemAmountDTO orderItemAmountDTO = new CalculateOrderAmountDTO.OrderItemAmountDTO();
            orderItemAmountDTO.setOrderId(orderId);
            orderItemAmountDTO.setProductType(orderItem.getProductType());
            orderItemAmountDTO.setSkuCode(orderItem.getSkuCode());
            orderItemAmountDTO.setSaleQuantity(orderItem.getSaleQuantity());
            orderItemAmountDTO.setSalePrice(orderItem.getSalePrice());
            orderItemAmountDTO.setAmountType(AmountTypeEnum.ORIGIN_PAY_AMOUNT.getCode());
            orderItemAmountDTO.setAmount(orderItemAmount);
            orderAmountDetailList.add(orderItemAmountDTO);

            CalculateOrderAmountDTO.OrderItemAmountDTO orderItemDiscountAmountDTO = new CalculateOrderAmountDTO.OrderItemAmountDTO();
            orderItemDiscountAmountDTO.setOrderId(orderId);
            orderItemAmountDTO.setProductType(orderItem.getProductType());
            orderItemDiscountAmountDTO.setSkuCode(orderItem.getSkuCode());
            orderItemDiscountAmountDTO.setSaleQuantity(orderItem.getSaleQuantity());
            orderItemDiscountAmountDTO.setSalePrice(orderItem.getSalePrice());
            orderItemDiscountAmountDTO.setAmountType(AmountTypeEnum.COUPON_DISCOUNT_AMOUNT.getCode());
            orderItemDiscountAmountDTO.setAmount(orderItemDiscountAmount);
            orderAmountDetailList.add(orderItemDiscountAmountDTO);

            CalculateOrderAmountDTO.OrderItemAmountDTO orderItemPayAmountDTO = new CalculateOrderAmountDTO.OrderItemAmountDTO();
            orderItemPayAmountDTO.setOrderId(orderId);
            orderItemAmountDTO.setProductType(orderItem.getProductType());
            orderItemPayAmountDTO.setSkuCode(orderItem.getSkuCode());
            orderItemPayAmountDTO.setSaleQuantity(orderItem.getSaleQuantity());
            orderItemPayAmountDTO.setSalePrice(orderItem.getSalePrice());
            orderItemPayAmountDTO.setAmountType(AmountTypeEnum.REAL_PAY_AMOUNT.getCode());
            orderItemPayAmountDTO.setAmount(orderItemPayAmount);
            orderAmountDetailList.add(orderItemPayAmountDTO);

            // 4.5. 对分摊后的金额进行重新求和
            reTotalOrderAmount = reTotalOrderAmount.add(orderItemAmount);
            reTotalDiscountAmount = reTotalDiscountAmount.add(orderItemDiscountAmount);
            reTalRealPayAmount = reTalRealPayAmount.add(orderItemPayAmount);
        }


        // 5. 总的实付金额还要加上运费
        BigDecimal shippingAmount = this.calculateShippingAmount(regionId, totalOrderAmount);
        reTalRealPayAmount = reTalRealPayAmount.add(shippingAmount);

        // 6. 封装响应体
        CalculateOrderAmountDTO calculateOrderAmountDTO = new CalculateOrderAmountDTO();
        // calculateOrderAmountDTO.setUserCoupon(); // TODO 返回使用的优惠券信息
        calculateOrderAmountDTO.setOrderAmountList(Arrays.asList(
            new CalculateOrderAmountDTO.OrderAmountDTO(orderId, AmountTypeEnum.ORIGIN_PAY_AMOUNT.getCode(), reTotalOrderAmount),
            new CalculateOrderAmountDTO.OrderAmountDTO(orderId, AmountTypeEnum.COUPON_DISCOUNT_AMOUNT.getCode(), reTotalDiscountAmount),
            new CalculateOrderAmountDTO.OrderAmountDTO(orderId, AmountTypeEnum.REAL_PAY_AMOUNT.getCode(), reTalRealPayAmount)
        ));
        calculateOrderAmountDTO.setOrderItemAmountList(orderAmountDetailList);

        return calculateOrderAmountDTO;
    }

    /**
     * 计算订单价格入参检查
     */
    private void check(CalculateOrderAmountQuery query) {
        // 订单编号
        String orderId = query.getOrderId();
        // 用户编号
        String userId = query.getUserId();
        if (StringUtils.isAnyEmpty(orderId, userId)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
        // 订单商品条目
        List<CalculateOrderAmountQuery.OrderItem> orderItemList = query.getOrderItemList();
        if (CollectionUtils.isEmpty(orderItemList)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
        // 订单费用信息
        List<CalculateOrderAmountQuery.OrderAmount> orderAmountList = query.getOrderAmountList();
        if (CollectionUtils.isEmpty(orderAmountList)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }
    }

    /**
     * 获取用户的优惠券的优惠金额
     */
    private BigDecimal getCouponDiscountAmount(String orderNo, String userNo, String couponNo) {
        if (StringUtils.isEmpty(couponNo)) {
            log.info("订单号:{}没有使用优惠券, 优惠金额为0", orderNo);
            return BigDecimal.ZERO;
        }
        CouponDO couponDO = couponMapper.selectOneByUserIdAndCouponId(userNo, couponNo);
        if (couponDO == null) {
            throw MarketExceptionEnum.USER_COUPON_IS_NULL.msg();
        }
        BigDecimal couponDiscountAmount = couponDO.getAmount();
        log.info("订单号:{}没有使用优惠券, 优惠金额为{}", orderNo, couponDiscountAmount);
        return couponDiscountAmount;
    }

    /**
     * 计算订单运费
     *
     * @param regionNo         区域编号
     * @param totalOrderAmount 订单费用
     */
    private BigDecimal calculateShippingAmount(String regionNo, BigDecimal totalOrderAmount) {
        // 运费
        BigDecimal shippingAmount;
        // 满多少免运费
        BigDecimal conditionAmount;

        // 查找运费模板
        FreightTemplateDO freightTemplateDO = freightTemplateMapper.selectOneByRegionId(regionNo);
        if (freightTemplateDO != null) {
            shippingAmount = freightTemplateDO.getShippingAmount();
            conditionAmount = freightTemplateDO.getConditionAmount();
        } else {
            shippingAmount = marketProperties.getDefaultShippingAmount();
            conditionAmount = marketProperties.getDefaultConditionAmount();
        }

        // 如果原单金额大于指定值则免运费
        if (totalOrderAmount.compareTo(conditionAmount) >= 0) {
            shippingAmount = BigDecimal.ZERO;
        }
        return shippingAmount;
    }

}
