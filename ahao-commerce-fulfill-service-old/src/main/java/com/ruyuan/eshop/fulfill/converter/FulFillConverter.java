package com.ruyuan.eshop.fulfill.converter;

import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillItemDO;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveOrderItemRequest;
import moe.ahao.commerce.tms.api.command.SendOutCommand;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface FulFillConverter {

    /**
     * 转换对象
     *
     * @param request 对象
     * @return 对象
     */
    OrderFulfillDO convertFulFillRequest(ReceiveFulfillRequest request);

    /**
     * 转换对象
     *
     * @param receiveOrderItem 对象
     * @return 对象
     */
    OrderFulfillItemDO convertFulFillRequest(ReceiveOrderItemRequest receiveOrderItem);

    /**
     * 转换对象
     *
     * @param receiveOrderItems 对象
     * @return 对象
     */
    List<OrderFulfillItemDO> convertFulFillRequest(List<ReceiveOrderItemRequest> receiveOrderItems);

    /**
     * 转换对象
     *
     * @param fulfillRequest 对象
     * @return 对象
     */
    SendOutCommand convertReceiveFulfillRequest(ReceiveFulfillRequest fulfillRequest);

    /**
     * 转换对象
     *
     * @param receiveOrderItem 对象
     * @return 对象
     */
    SendOutCommand.OrderItem convertSendOutOrderItemRequest(ReceiveOrderItemRequest receiveOrderItem);

    /**
     * 转换对象
     *
     * @param receiveOrderItems 对象
     * @return 对象
     */
    List<SendOutCommand.OrderItem> convertSendOutOrderItemRequest(List<ReceiveOrderItemRequest> receiveOrderItems);

    /**
     * 转换对象
     *
     * @param fulfillRequest 对象
     * @return 对象
     */
    PickGoodsCommand convertPickGoodsRequest(ReceiveFulfillRequest fulfillRequest);

    /**
     * 转换对象
     *
     * @param receiveOrderItem 对象
     * @return 对象
     */
    PickGoodsCommand.OrderItem convertPickOrderItemRequest(ReceiveOrderItemRequest receiveOrderItem);

    /**
     * 转换对象
     *
     * @param receiveOrderItems 对象
     * @return 对象
     */
    List<PickGoodsCommand.OrderItem> convertPickOrderItemRequest(List<ReceiveOrderItemRequest> receiveOrderItems);
}
