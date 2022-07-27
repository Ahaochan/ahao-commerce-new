package moe.ahao.commerce.aftersale.api;

import moe.ahao.commerce.aftersale.api.dto.AfterSaleOrderDetailDTO;
import moe.ahao.commerce.aftersale.api.dto.AfterSaleOrderListDTO;
import moe.ahao.commerce.aftersale.api.query.AfterSaleQuery;
import moe.ahao.domain.entity.PagingInfo;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单中心-售后查询业务接口
 */
public interface AfterSaleQueryFeignApi {
    String PATH = "/api/aftersale";
    /**
     * 查询售后列表
     */
    @PostMapping("/listAfterSales")
    Result<PagingInfo<AfterSaleOrderListDTO>> listAfterSales(@RequestBody AfterSaleQuery query);

    /**
     * 查询售后单详情
     */
    @PostMapping("/afterSaleDetail")
    Result<AfterSaleOrderDetailDTO> afterSaleDetail(String afterSaleId);

}
