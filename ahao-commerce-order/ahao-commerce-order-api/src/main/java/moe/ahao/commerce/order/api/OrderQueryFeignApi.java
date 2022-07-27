package moe.ahao.commerce.order.api;

import moe.ahao.commerce.order.api.dto.OrderDetailDTO;
import moe.ahao.commerce.order.api.dto.OrderListDTO;
import moe.ahao.commerce.order.api.query.OrderQuery;
import moe.ahao.domain.entity.PagingInfo;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单中心-订单查询业务接口
 */
public interface OrderQueryFeignApi {
    String PATH = "/api/order";

    /**
     * 查询订单列表
     */
    @PostMapping("/listOrders")
    Result<PagingInfo<OrderListDTO>> listOrders(@RequestBody OrderQuery query);

    /**
     * 查询订单详情
     */
    @PostMapping("/orderDetail")
    Result<OrderDetailDTO> orderDetail(String orderId);
}
