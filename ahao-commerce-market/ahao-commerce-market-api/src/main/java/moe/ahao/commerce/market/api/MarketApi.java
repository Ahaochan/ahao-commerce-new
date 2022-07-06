package moe.ahao.commerce.market.api;

import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.domain.entity.Result;

public interface MarketApi {
    /**
     * 计算订单费用
     */
    Result<CalculateOrderAmountDTO> calculateOrderAmount(CalculateOrderAmountQuery query);
}
