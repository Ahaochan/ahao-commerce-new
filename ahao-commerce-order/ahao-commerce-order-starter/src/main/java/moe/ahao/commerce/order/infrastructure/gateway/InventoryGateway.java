package moe.ahao.commerce.order.infrastructure.gateway;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;
import moe.ahao.commerce.order.infrastructure.gateway.feign.InventoryFeignClient;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 库存服务远程接口
 */
@Component
public class InventoryGateway {
    /**
     * 库存服务
     */
    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    /**
     * 扣减订单条目库存
     */
    @SentinelResource(value = "InventoryRemote:deductProductStock")
    public void deductProductStock(DeductProductStockCommand lockProductStockRequest) {
        Result<Boolean> result = inventoryFeignClient.deductProductStock(lockProductStockRequest);
        // 检查锁定商品库存结果
        if (result.getCode() != moe.ahao.domain.entity.Result.SUCCESS) {
            throw new OrderException(result.getCode(), result.getMsg());
        }
    }
}
