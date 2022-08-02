package com.ruyuan.eshop.order.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求入参
 *
 * 订单交易核心数据模型：你是谁、你在哪里、你要买什么、你要付多少钱、打算怎么支付、订单相关的其他信息
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class CreateOrderRequest implements Serializable {

    private static final long serialVersionUID = -3719117561480569064L;

    /**
     * 订单号，生成订单号那里生成出来的
     */
    private String orderId;

    /**
     * 业务线标识，哪个业务线来进行生单，生鲜、外卖、新零售、第三方商家、社区团购，交易系统 -> 交易平台
     */
    private Integer businessIdentifier;

    // 用户信息
    /**
     * 微信openid，如果你是用户账号 跟微信做了绑定的，你的微信账号openid也会过来，下单的用户，他的微信账号id
     */
    private String openid;
    /**
     * 用户ID，系统里的用户账号id
     */
    private String userId;

    // 订单主体信息
    /**
     * 订单类型，留着后续进行扩展的，订单可以有很多类型的，普通订单、秒杀订单、预售订单、虚拟订单，订单类型随着业务的丰富，是可以有很多种
     */
    private Integer orderType;

    /**
     * 卖家ID，B2B2C平台，你是一个平台，大B，平台里有很多的小B商家，在你的平台里开店铺，对外出售商品
     * 用户在下单的时候，在你的平台里，浏览不同商家的店铺，在里面购买商品，京东、天猫、淘宝，都是这样子的，针对的是哪个卖家店铺的商品进行下单
     */
    private String sellerId;

    /**
     * 用户备注，下单的时候一般会给你空间去填写你对订单的备注
     */
    private String userRemark;

    /**
     * 优惠券ID，每次下单可以使用最多一个优惠券，本次下单是否使用优惠券，以及使用的优惠券id是什么
     */
    private String couponId;

    // 收货地址信息

    /**
     * 提货方式，配送上门和自提，提货方式都支持很多了，最近的菜鸟驿站、丰巢货柜去提货
     */
    private Integer deliveryType;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String area;

    /**
     * 街道
     */
    private String street;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 经度
     */
    private BigDecimal lon;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 用户收货地址id，收货地址是可以有一个专门的表，用户可以维护和管理自己的多个收货地址
     * 在下单的时候，可以填写一个新的收货地址，也可以选择已有的收货地址，可以订单绑定到一个收货地址表id
     */
    private String userAddressId;

    /**
     * 地区编码，你所在地区的编号
     */
    private String addressCode;

    /**
     * 区域ID，你的区域是有一个自己的id，地址数据在系统里也可以有一个全面的地址数据表
     */
    private String regionId;

    /**
     * 配送区域ID，跟你公司配送区域表里的一条数据进行绑定，对应的是哪个配送区域，就跟id做一个绑定就可以了
     */
    private String shippingAreaId;

    // 下单客户端信息，主要用于风控检查

    /**
     * 客户端ip
     */
    private String clientIp;
    /**
     * 设备编号
     */
    private String deviceId;

    /**
     * 订单商品信息，一个订单可以对多个商品进行下单
     */
    private List<OrderItemRequest> orderItemRequestList;
    /**
     * 订单费用信息，一个订单是可以有多条费用数据
     */
    private List<OrderAmountRequest> orderAmountRequestList;
    /**
     * 支付信息，支付数据是可以有多条的，灵活的支持不同的支付类型有不同的支付金额
     * 很常见的，PayType可以包含账户余额的抵扣，一部分钱，微信支付去付款剩下的钱，积分抵扣部分钱
     * 支付数据是可以多条的
     */
    private List<PaymentRequest> paymentRequestList;

    /**
     * 订单条目信息
     */
    @Data
    public static class OrderItemRequest implements Serializable {

        private static final long serialVersionUID = 8267460170612816097L;

        /**
         * 商品类型，普通商品和预售商品，虚拟商品，服务商品
         */
        private Integer productType;

        /**
         * 销售数量
         */
        private Integer saleQuantity;

        /**
         * sku编号
         */
        private String skuCode;
    }

    /**
     * 订单费用信息
     */
    @Data
    public static class OrderAmountRequest implements Serializable {

        private static final long serialVersionUID = -8189987703740512851L;

        /**
         * 费用类型
         */
        private Integer amountType;

        /**
         * 费用金额
         */
        private Integer amount;
    }

    /**
     * 订单支付信息
     */
    @Data
    public static class PaymentRequest implements Serializable {


        private static final long serialVersionUID = -1821079125013490176L;

        /**
         * 支付类型，支付类型是可以有很多种
         */
        private Integer payType;

        /**
         * 账户类型
         */
        private Integer accountType;

    }
}