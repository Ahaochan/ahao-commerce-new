package moe.ahao.commerce.order.infrastructure.wms;


import moe.ahao.commerce.order.infrastructure.domain.dto.WmsShipDTO;
import moe.ahao.commerce.order.infrastructure.exception.OrderException;

/**
 * 订单物流配送结果处理器
 */
public interface OrderWmsShipResultProcessor {

    /**
     * 执行具体的业务逻辑
     *
     * @throws OrderException
     */
    void execute(WmsShipDTO wmsShipDTO) throws OrderException;
}
