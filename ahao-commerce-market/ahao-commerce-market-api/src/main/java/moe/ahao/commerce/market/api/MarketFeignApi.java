package moe.ahao.commerce.market.api;

import moe.ahao.commerce.market.api.dto.CalculateOrderAmountDTO;
import moe.ahao.commerce.market.api.query.CalculateOrderAmountQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface MarketFeignApi extends MarketApi {
    String CONTEXT = "/api/market/";

    @PostMapping("/calculateOrderAmount")
    Result<CalculateOrderAmountDTO> calculateOrderAmount(@RequestBody CalculateOrderAmountQuery query);
}
