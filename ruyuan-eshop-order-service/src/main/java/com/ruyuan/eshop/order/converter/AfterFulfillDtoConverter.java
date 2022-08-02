package com.ruyuan.eshop.order.converter;

import com.ruyuan.eshop.fulfill.domain.event.OrderDeliveredEvent;
import com.ruyuan.eshop.fulfill.domain.event.OrderOutStockEvent;
import com.ruyuan.eshop.fulfill.domain.event.OrderSignedEvent;
import com.ruyuan.eshop.order.domain.dto.AfterFulfillDTO;
import org.mapstruct.Mapper;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface AfterFulfillDtoConverter {

    /**
     * 对象转换
     *
     * @param event 对象
     * @return 对象
     */
    AfterFulfillDTO convert(OrderOutStockEvent event);

    /**
     * 对象转换
     *
     * @param event 对象
     * @return 对象
     */
    AfterFulfillDTO convert(OrderDeliveredEvent event);

    /**
     * 对象转换
     *
     * @param event 对象
     * @return 对象
     */
    AfterFulfillDTO convert(OrderSignedEvent event);

}
