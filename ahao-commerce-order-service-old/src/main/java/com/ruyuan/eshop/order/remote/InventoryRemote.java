package com.ruyuan.eshop.order.remote;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.ruyuan.eshop.order.exception.OrderBizException;
import moe.ahao.commerce.inventory.api.InventoryFeignApi;
import moe.ahao.commerce.inventory.api.command.DeductProductStockCommand;
import moe.ahao.domain.entity.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;


/**
 * 库存服务远程接口
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class InventoryRemote {

    /**
     * 库存服务
     */
    @DubboReference(version = "1.0.0", retries = 0)
    private InventoryFeignApi inventoryApi;

    /**
     * 扣减订单条目库存
     * @param lockProductStockRequest
     */
    @SentinelResource(value = "InventoryRemote:deductProductStock")
    public void deductProductStock(DeductProductStockCommand lockProductStockRequest) {
        Result<Boolean> result = inventoryApi.deductProductStock(lockProductStockRequest);
        // 检查锁定商品库存结果
        if (result.getCode() != moe.ahao.domain.entity.Result.SUCCESS) {
            throw new OrderBizException(String.valueOf(result.getCode()), result.getMsg());
        }
    }
}
