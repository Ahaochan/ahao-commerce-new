package moe.ahao.commerce.order.api;

import moe.ahao.commerce.order.api.command.*;
import moe.ahao.commerce.order.api.dto.CreateOrderDTO;
import moe.ahao.commerce.order.api.dto.PrePayOrderDTO;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单中心-正向下单业务接口
 */
public interface OrderFeignApi {
    String PATH = "/api/order";

    /**
     * 生成订单号接口
     */
    @PostMapping("/genOrderId")
    Result<String> genOrderId(@RequestBody GenOrderIdCommand command);

    /**
     * 提交订单接口
     */
    @PostMapping("/createOrder")
    Result<CreateOrderDTO> createOrder(@RequestBody CreateOrderCommand command);

    /**
     * 预支付订单接口
     *
     * @param command 预支付订单请求入参
     */
    @PostMapping("/prePayOrder")
    Result<PrePayOrderDTO> prePayOrder(@RequestBody PrePayOrderCommand command);

    /**
     * 支付回调接口
     *
     * @param command 支付系统回调入参
     */
    @PostMapping("/payCallback")
    Result<Boolean> payCallback(@RequestBody PayCallbackCommand command);

    /**
     * 移除订单
     *
     * @param command 移除订单请求入参
     */
    @PostMapping("/removeOrders")
    Result<Boolean> removeOrders(@RequestBody RemoveOrderCommand command);

    /**
     * 修改地址
     *
     * @param command 修改地址请求入参
     */
    @PostMapping("/adjustDeliveryAddress")
    Result<Boolean> adjustDeliveryAddress(@RequestBody AdjustDeliveryAddressCommand command);
}
