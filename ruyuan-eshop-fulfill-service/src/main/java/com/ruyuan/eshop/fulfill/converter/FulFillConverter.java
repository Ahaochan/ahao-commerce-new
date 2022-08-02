package com.ruyuan.eshop.fulfill.converter;

import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillDO;
import com.ruyuan.eshop.fulfill.domain.entity.OrderFulfillItemDO;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveFulfillRequest;
import com.ruyuan.eshop.fulfill.domain.request.ReceiveOrderItemRequest;
import com.ruyuan.eshop.fulfill.dto.OrderFulfillDTO;
import com.ruyuan.eshop.tms.domain.SendOutRequest;
import com.ruyuan.eshop.wms.domain.PickGoodsRequest;
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
     * @param orderFulfillDO 对象
     * @return 对象
     */
    OrderFulfillDTO convertToOrderFulfillDTO(OrderFulfillDO orderFulfillDO);

    /**
     * 转换对象
     *
     * @param orderFulfillDOs 对象
     * @return 对象
     */
    List<OrderFulfillDTO> convertToOrderFulfillDTOs(List<OrderFulfillDO> orderFulfillDOs);

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
    SendOutRequest convertReceiveFulfillRequest(ReceiveFulfillRequest fulfillRequest);

    /**
     * 转换对象
     *
     * @param receiveOrderItem 对象
     * @return 对象
     */
    SendOutRequest.OrderItemRequest convertSendOutOrderItemRequest(ReceiveOrderItemRequest receiveOrderItem);

    /**
     * 转换对象
     *
     * @param receiveOrderItems 对象
     * @return 对象
     */
    List<SendOutRequest.OrderItemRequest> convertSendOutOrderItemRequest(List<ReceiveOrderItemRequest> receiveOrderItems);

    /**
     * 转换对象
     *
     * @param orderFulfill 对象
     * @return 对象
     */
    SendOutRequest convertSendOutRequest(OrderFulfillDO orderFulfill);

    /**
     * 转换对象
     *
     * @param orderFulfillItem 对象
     * @return 对象
     */
    SendOutRequest.OrderItemRequest convertSendOutOrderItemRequest(OrderFulfillItemDO orderFulfillItem);

    /**
     * 转换对象
     *
     * @param orderFulfillItems 对象
     * @return 对象
     */
    List<SendOutRequest.OrderItemRequest> convertSendOutOrderItemRequests(List<OrderFulfillItemDO> orderFulfillItems);


    /**
     * 转换对象
     *
     * @param fulfillRequest 对象
     * @return 对象
     */
    PickGoodsRequest convertPickGoodsRequest(ReceiveFulfillRequest fulfillRequest);


    /**
     * 转换对象
     *
     * @param orderFulfill 对象
     * @return 对象
     */
    PickGoodsRequest convertPickGoodsRequest(OrderFulfillDO orderFulfill);

    /**
     * 转换对象
     *
     * @param orderFulfillItem 对象
     * @return 对象
     */
    PickGoodsRequest.OrderItemRequest convertPickOrderItemRequest(OrderFulfillItemDO orderFulfillItem);

    /**
     * 转换对象
     *
     * @param orderFulfillItems 对象
     * @return 对象
     */
    List<PickGoodsRequest.OrderItemRequest> convertPickOrderItemRequests(List<OrderFulfillItemDO> orderFulfillItems);

    /**
     * 转换对象
     *
     * @param receiveOrderItem 对象
     * @return 对象
     */
    PickGoodsRequest.OrderItemRequest convertPickOrderItemRequest(ReceiveOrderItemRequest receiveOrderItem);

    /**
     * 转换对象
     *
     * @param receiveOrderItems 对象
     * @return 对象
     */
    List<PickGoodsRequest.OrderItemRequest> convertPickOrderItemRequest(List<ReceiveOrderItemRequest> receiveOrderItems);

}
