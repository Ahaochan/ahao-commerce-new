package moe.ahao.commerce.order.infrastructure.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.gateway.feign.MarketFeignClient;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 营销服务远程接口
 */
@Component
public class MarketCalculateGateway {
    @Autowired
    private MarketFeignClient marketFeignClient;

    /**
     * 计算订单费用
     */
    @SentinelResource(value = "MarketCalculateGateway:calculateOrderAmount")
    public CalculateOrderAmountDTO calculateOrderAmount(CalculateOrderAmountQuery query) {
        Result<CalculateOrderAmountDTO> result = marketFeignClient.calculateOrderAmount(query);
        // 检查价格计算结果
        if (result.getCode() != Result.SUCCESS) {
            throw new OrderException(result.getCode(), result.getMsg());
        }
        return result.getObj();
    }
}
