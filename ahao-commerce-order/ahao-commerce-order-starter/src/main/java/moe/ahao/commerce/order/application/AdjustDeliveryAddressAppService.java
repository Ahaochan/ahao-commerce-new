package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.OrderStatusEnum;
import moe.ahao.commerce.order.api.command.AdjustDeliveryAddressCommand;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderDeliveryDetailDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderInfoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderDeliveryDetailMapper;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdjustDeliveryAddressAppService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDeliveryDetailMapper orderDeliveryDetailMapper;

    public boolean adjustDeliveryAddress(AdjustDeliveryAddressCommand command) {
        // 1. 参数校验
        this.check(command);

        // 2. 更新配送地址信息
        orderDeliveryDetailMapper.updateDeliveryAddressByOrderId(command.getOrderId(),
            command.getProvince(), command.getCity(), command.getArea(), command.getStreet(), command.getDetailAddress(),
            command.getLat(), command.getLon());

        return true;
    }

    private void check(AdjustDeliveryAddressCommand command) {
        // 1. 参数校验
        String orderId = command.getOrderId();
        if (StringUtils.isEmpty(orderId)) {
            throw OrderExceptionEnum.ORDER_ID_IS_NULL.msg();
        }

        // 2. 根据id查询订单
        OrderInfoDO orderInfo = orderInfoMapper.selectOneByOrderId(orderId);
        if (orderInfo == null) {
            throw OrderExceptionEnum.ORDER_NOT_FOUND.msg();
        }

        // 3. 校验订单是否未出库
        if (!OrderStatusEnum.unOutStockStatus().contains(orderInfo.getOrderStatus())) {
            throw OrderExceptionEnum.ORDER_NOT_ALLOW_TO_ADJUST_ADDRESS.msg();
        }

        // 4. 查询订单配送信息
        OrderDeliveryDetailDO orderDeliveryDetail = orderDeliveryDetailMapper.selectOneByOrderId(orderId);
        if (orderDeliveryDetail == null) {
            throw OrderExceptionEnum.ORDER_DELIVERY_NOT_FOUND.msg();
        }

        // 5. 校验配送信息是否已经被修改过一次
        if (orderDeliveryDetail.getModifyAddressCount() > 0) {
            throw OrderExceptionEnum.ORDER_DELIVERY_ADDRESS_HAS_BEEN_ADJUSTED.msg();
        }
    }
}
