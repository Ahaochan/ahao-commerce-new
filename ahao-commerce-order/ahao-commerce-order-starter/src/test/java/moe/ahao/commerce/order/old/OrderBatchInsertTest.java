package moe.ahao.commerce.order.old;

import com.google.common.collect.Lists;
import moe.ahao.commerce.order.OrderApplication;
import moe.ahao.commerce.order.api.command.GenOrderIdCommand;
import moe.ahao.commerce.order.application.GenOrderIdAppService;
import moe.ahao.commerce.order.infrastructure.config.OrderProperties;
import moe.ahao.commerce.order.infrastructure.enums.BusinessIdentifierEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderIdTypeEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderTypeEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderDeliveryDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderItemDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderPaymentDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service.OrderDeliveryDetailMybatisService;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service.OrderInfoMybatisService;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service.OrderItemMybatisService;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.service.OrderPaymentDetailMybatisService;
import moe.ahao.util.commons.lang.RandomHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = OrderApplication.class)
public class OrderBatchInsertTest {
    @Autowired
    private GenOrderIdAppService genOrderIdAppService;

    @Autowired
    private OrderProperties orderProperties;

    @Autowired
    private OrderInfoMybatisService orderInfoMybatisService;

    @Autowired
    private OrderItemMybatisService orderItemMybatisService;

    @Autowired
    private OrderPaymentDetailMybatisService orderPaymentDetailMybatisService;

    @Autowired
    private OrderDeliveryDetailMybatisService orderDeliveryDetailMybatisService;

    /**
     * ????????????"??????????????????"????????????????????????
     */
    @Test
    public void executeListQueryTestData() {
        List<OrderInfoDO> orderInfoDOList = Lists.newArrayList();
        List<OrderItemDO> orderItemDOList = Lists.newArrayList();
        List<OrderPaymentDetailDO> orderPaymentDetailDOList = Lists.newArrayList();
        List<OrderDeliveryDetailDO> orderDeliveryDetailDOList = Lists.newArrayList();

        //  ????????????
        for (int i = 0; i < 10000; i++) {
            //  ???????????????
            String userId = String.valueOf(ThreadLocalRandom.current().nextInt(0, 1000));
            String orderId = genOrderIdAppService.generate(new GenOrderIdCommand(BusinessIdentifierEnum.SELF_MALL.getCode(), OrderIdTypeEnum.SALE_ORDER.getCode(), userId));

            OrderInfoDO orderInfoDO = buildOrderInfo(orderId, userId);
            OrderItemDO orderItemDO = buildOrderItem(orderId, userId);
            OrderPaymentDetailDO paymentDetailDO = buildOrderPaymentDetail(orderId, orderInfoDO);
            OrderDeliveryDetailDO orderDeliveryDetailDO = buildOrderDeliveryDetail(orderId);

            orderInfoDOList.add(orderInfoDO);
            orderItemDOList.add(orderItemDO);
            orderPaymentDetailDOList.add(paymentDetailDO);
            orderDeliveryDetailDOList.add(orderDeliveryDetailDO);
        }

        //  ????????????
        long startTime = System.currentTimeMillis();
        orderInfoMybatisService.saveBatch(orderInfoDOList);
        orderItemMybatisService.saveBatch(orderItemDOList);
        orderPaymentDetailMybatisService.saveBatch(orderPaymentDetailDOList);
        orderDeliveryDetailMybatisService.saveBatch(orderDeliveryDetailDOList);
        long endTime = System.currentTimeMillis();
    }

    private OrderDeliveryDetailDO buildOrderDeliveryDetail(String orderId) {
        //  ??????order_delivery_detail??????
        OrderDeliveryDetailDO orderDeliveryDetailDO = new OrderDeliveryDetailDO();
        orderDeliveryDetailDO.setOrderId(orderId);
        orderDeliveryDetailDO.setDeliveryType(1);
        orderDeliveryDetailDO.setProvince("110000");
        orderDeliveryDetailDO.setCity("110100");
        orderDeliveryDetailDO.setArea("110105");
        orderDeliveryDetailDO.setStreet("110101007");
        orderDeliveryDetailDO.setDetailAddress("????????????");
        orderDeliveryDetailDO.setLon(new BigDecimal("100.1000000000"));
        orderDeliveryDetailDO.setLat(new BigDecimal("1010.2010100000"));
        orderDeliveryDetailDO.setReceiverName("????????????");
        orderDeliveryDetailDO.setReceiverPhone("13434545545");
        orderDeliveryDetailDO.setModifyAddressCount(0);
        orderDeliveryDetailDO.setDelivererNo(null);
        orderDeliveryDetailDO.setDelivererName(null);
        orderDeliveryDetailDO.setDelivererPhone(null);
        orderDeliveryDetailDO.setOutStockTime(null);
        orderDeliveryDetailDO.setSignedTime(null);

        return orderDeliveryDetailDO;
    }

    private OrderPaymentDetailDO buildOrderPaymentDetail(String orderId, OrderInfoDO orderInfoDO) {
        //  ??????order_payment_detail??????
        OrderPaymentDetailDO paymentDetailDO = new OrderPaymentDetailDO();
        paymentDetailDO.setOrderId(orderId);
        paymentDetailDO.setAccountType(1);
        paymentDetailDO.setPayType(orderInfoDO.getPayType());

        //  ????????????
        if (orderInfoDO.getOrderStatus() > 10) {
            paymentDetailDO.setPayStatus(20);
        } else {
            paymentDetailDO.setPayStatus(10);
        }
        paymentDetailDO.setPayAmount(orderInfoDO.getPayAmount());
        paymentDetailDO.setPayTime(orderInfoDO.getPayTime());
        paymentDetailDO.setOutTradeNo(RandomHelper.getString(19, RandomHelper.DIST_NUMBER));
        paymentDetailDO.setPayRemark("????????????");

        return paymentDetailDO;
    }

    private OrderItemDO buildOrderItem(String orderId, String userId) {
        //  ??????order_item??????
        OrderItemDO orderItemDO = new OrderItemDO();
        orderItemDO.setOrderId(orderId);
        orderItemDO.setOrderItemId(orderId + "001");
        //  ???????????? 1:????????????,2:????????????
        orderItemDO.setProductType(1);
        orderItemDO.setProductId("1001010");
        orderItemDO.setProductImg("test.img");
        orderItemDO.setProductName("????????????");
        orderItemDO.setSkuCode("10101010");
        orderItemDO.setSaleQuantity(new BigDecimal(10));
        orderItemDO.setSalePrice(new BigDecimal("10"));
        orderItemDO.setOriginAmount(new BigDecimal("100"));
        orderItemDO.setPayAmount(new BigDecimal("95.05"));
        orderItemDO.setProductUnit("???");
        orderItemDO.setPurchasePrice(new BigDecimal("0.05"));
        orderItemDO.setSellerId(userId + "000");

        return orderItemDO;
    }

    private OrderInfoDO buildOrderInfo(String orderId, String userId) {
        //  ??????order_info??????
        OrderInfoDO orderInfoDO = new OrderInfoDO();
        orderInfoDO.setBusinessIdentifier(1);
        orderInfoDO.setOrderId(orderId);
        orderInfoDO.setParentOrderId(null);
        orderInfoDO.setBusinessOrderId(null);
        orderInfoDO.setOrderType(OrderTypeEnum.NORMAL.getCode());
        /*  ???????????? OrderStatusEnum
        CREATED(10, "?????????"), PAID(20, "?????????"),    FULFILL(30, "?????????"),
        OUT_STOCK(40, "??????"),    DELIVERY(50, "?????????"),
        SIGNED(60, "?????????"),  CANCELED(70, "?????????"),
        REFUSED(100, "?????????"),    INVALID(127, "????????????"); */
        int[] orderStatusArray = {10, 20, 30, 40, 50, 60, 70, 100, 127};
        int orderStatusNum = ThreadLocalRandom.current().nextInt(orderStatusArray.length);
        orderInfoDO.setOrderStatus(orderStatusArray[orderStatusNum]);
        orderInfoDO.setCancelType(null);
        orderInfoDO.setCancelTime(null);
        orderInfoDO.setSellerId(userId + "000");
        orderInfoDO.setUserId(userId);
        orderInfoDO.setTotalAmount(new BigDecimal("100"));
        orderInfoDO.setPayAmount(new BigDecimal("96"));
        /*
           ????????????  10:????????????, 20:???????????????
         */
        int[] payTypeArray = {10, 20};
        int payTypeNum = ThreadLocalRandom.current().nextInt(payTypeArray.length);
        orderInfoDO.setPayType(payTypeArray[payTypeNum]);
        orderInfoDO.setCouponId("1001001");
        //  ????????????
        if (orderInfoDO.getOrderStatus() > 10) {
            orderInfoDO.setPayTime(new Date());
        } else {
            orderInfoDO.setPayTime(null);
        }

        long currentTimeMillis = System.currentTimeMillis();
        Integer expireTime = orderProperties.getExpireTime();
        orderInfoDO.setExpireTime(new Date(currentTimeMillis + expireTime));

        orderInfoDO.setUserRemark("????????????");
        orderInfoDO.setDeleteStatus(0);
        orderInfoDO.setCommentStatus(0);
        orderInfoDO.setExtJson(null);

        return orderInfoDO;
    }
}
