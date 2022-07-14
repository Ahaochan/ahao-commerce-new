package moe.ahao.commerce.fulfill.application.saga;

import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;

/**
 * wms的saga service
 */
public interface WmsSagaService {
    /**
     * 捡货
     */
    Boolean pickGoods(ReceiveFulfillCommand command);

    /**
     * 捡货补偿
     */
    Boolean pickGoodsCompensate(ReceiveFulfillCommand command);
}
