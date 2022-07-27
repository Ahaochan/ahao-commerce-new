package moe.ahao.commerce.order.adapter.http;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.order.api.OrderFeignApi;
import moe.ahao.commerce.order.api.command.*;
import moe.ahao.commerce.order.api.dto.CreateOrderDTO;
import moe.ahao.commerce.order.api.dto.PrePayOrderDTO;
import moe.ahao.commerce.order.application.*;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 订单中心接口
 */
@Slf4j
@RestController
@RequestMapping(OrderFeignApi.PATH)
public class  OrderController implements OrderFeignApi {
    @Autowired
    private GenOrderIdAppService genOrderIdAppService;
    @Autowired
    private CreateOrderAppService createOrderAppService;
    @Autowired
    private PrePayOrderAppService prePayOrderAppService;
    @Autowired
    private PayCallbackAppService payCallbackAppService;
    @Autowired
    private RemoveOrderAppService removeOrderAppService;
    @Autowired
    private AdjustDeliveryAddressAppService adjustDeliveryAddressAppService;

    /**
     * 生成订单号接口
     *
     * @param command 生成订单号入参
     * @return 订单号
     */
    @Override
    @SentinelResource("OrderController:genOrderId")
    public Result<String> genOrderId(@RequestBody GenOrderIdCommand command) {
        String orderId = genOrderIdAppService.generate(command);
        return Result.success("success", orderId);
    }

    /**
     * 提交订单/生成订单接口
     *
     * @param command 提交订单请求入参
     * @return 订单号
     */
    @Override
    @SentinelResource("OrderController:createOrder")
    public Result<CreateOrderDTO> createOrder(@RequestBody CreateOrderCommand command) {
        CreateOrderDTO createOrderDTO = createOrderAppService.createOrder(command);
        return Result.success(createOrderDTO);
    }

    /**
     * 预支付订单接口
     *
     * @param command 预支付订单请求入参
     */
    @Override
    @SentinelResource("OrderController:prePayOrder")
    public Result<PrePayOrderDTO> prePayOrder(@RequestBody PrePayOrderCommand command) {
        PrePayOrderDTO prePayOrderDTO = prePayOrderAppService.prePayOrder(command);
        return Result.success(prePayOrderDTO);
    }

    /**
     * 支付回调接口
     *
     * @param command 支付系统回调入参
     */
    @Override
    @SentinelResource("OrderController:payCallback")
    public Result<Boolean> payCallback(@RequestBody PayCallbackCommand command) {
        payCallbackAppService.payCallback(command);
        return Result.success(true);
    }

    /**
     * 移除订单
     *
     * @param command 移除订单请求入参
     */
    @Override
    public Result<Boolean> removeOrders(@RequestBody RemoveOrderCommand command) {
        Boolean success = removeOrderAppService.removeOrders(command);
        return Result.success(success);
    }

    /**
     * 修改地址
     *
     * @param command 修改地址请求入参
     */
    @Override
    public Result<Boolean> adjustDeliveryAddress(AdjustDeliveryAddressCommand command) {
        Boolean success = adjustDeliveryAddressAppService.adjustDeliveryAddress(command);
        return Result.success(success);
    }
}
