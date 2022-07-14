package moe.ahao.commerce.fulfill.application.saga;


import moe.ahao.commerce.fulfill.api.command.ReceiveFulfillCommand;

/**
 * fulfull saga service
 */
public interface FulfillSagaService {

    /**
     * 创建履约单
     */
    Boolean createFulfillOrder(ReceiveFulfillCommand command);

    /**
     * 补偿创建履约单
     */
    Boolean createFulfillOrderCompensate(ReceiveFulfillCommand command);
}
