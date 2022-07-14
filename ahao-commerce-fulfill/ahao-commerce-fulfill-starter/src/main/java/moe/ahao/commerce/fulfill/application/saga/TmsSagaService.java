package moe.ahao.commerce.fulfill.application.saga;


import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;

/**
 * tms saga service
 */
public interface TmsSagaService {
    /**
     * 发货
     */
    Boolean sendOut(ReceiveFulfillCommand command);

    /**
     * 发货补偿
     */
    Boolean sendOutCompensate(ReceiveFulfillCommand command);
}
