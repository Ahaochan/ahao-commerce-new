package moe.ahao.commerce.wms.application;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.wms.api.command.PickGoodsCommand;
import moe.ahao.commerce.wms.api.dto.PickDTO;
import moe.ahao.commerce.wms.infrastructure.exception.WmsExceptionEnum;
import moe.ahao.commerce.wms.infrastructure.gateway.impl.demo.data.ScheduleDeliveryDTO;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderDO;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.data.DeliveryOrderItemDO;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.mapper.DeliveryOrderMapper;
import moe.ahao.commerce.wms.infrastructure.repository.impl.mybatis.service.DeliveryOrderItemMybatisService;
import moe.ahao.util.commons.lang.RandomHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PickGoodsAppService {
    @Autowired
    private DeliveryOrderMapper deliveryOrderMapper;
    @Autowired
    private DeliveryOrderItemMybatisService deliveryOrderItemMybatisService;

    @Transactional(rollbackFor = Exception.class)
    public PickDTO pickGoods(PickGoodsCommand command) {
        String wmsException = command.getWmsException();
        if (StringUtils.isNotBlank(wmsException) && wmsException.equals("true")) {
            throw WmsExceptionEnum.EXCEPTION.msg();
        }

        // 1. 捡货，调度出库
        ScheduleDeliveryDTO result = this.scheduleDelivery(command);

        // 2. 存储出库单和出库单条目
        deliveryOrderMapper.insert(result.getDeliveryOrder());
        deliveryOrderItemMybatisService.saveBatch(result.getDeliveryOrderItems());

        // 3. 构造返回参数
        return new PickDTO(command.getOrderId());
    }

    /**
     * 调度出库
     */
    private ScheduleDeliveryDTO scheduleDelivery(PickGoodsCommand request) {
        log.info("调度出库, orderId: {}", request.getOrderId());
        // 1. 生成出库单
        DeliveryOrderDO deliveryOrder = new DeliveryOrderDO();
        deliveryOrder.setBusinessIdentifier(request.getBusinessIdentifier());
        deliveryOrder.setDeliveryOrderId(this.genDeliveryOrderId());
        deliveryOrder.setOrderId(request.getOrderId());
        deliveryOrder.setSellerId(request.getSellerId());
        deliveryOrder.setUserId(request.getUserId());
        deliveryOrder.setPayType(request.getPayType());
        deliveryOrder.setPayAmount(request.getPayAmount());
        deliveryOrder.setTotalAmount(request.getTotalAmount());
        deliveryOrder.setDeliveryAmount(request.getDeliveryAmount());

        // 2. 生成出库单条目
        List<DeliveryOrderItemDO> deliveryOrderItems = new ArrayList<>();
        for (PickGoodsCommand.OrderItem orderItem : request.getOrderItems()) {
            DeliveryOrderItemDO deliveryOrderItem = new DeliveryOrderItemDO();
            deliveryOrderItem.setDeliveryOrderId(deliveryOrder.getDeliveryOrderId());
            deliveryOrderItem.setSkuCode(orderItem.getSkuCode());
            deliveryOrderItem.setProductName(orderItem.getProductName());
            deliveryOrderItem.setSalePrice(orderItem.getSalePrice());
            deliveryOrderItem.setSaleQuantity(orderItem.getSaleQuantity());
            deliveryOrderItem.setProductUnit(orderItem.getProductUnit());
            deliveryOrderItem.setPayAmount(orderItem.getPayAmount());
            deliveryOrderItem.setOriginAmount(orderItem.getOriginAmount());
            // deliveryOrderItem.setPickingCount(orderItem.getSaleQuantity());
            // deliveryOrderItem.setSkuContainerId("1");

            deliveryOrderItems.add(deliveryOrderItem);
        }

        // 3. sku调度出库
        // 这里仅仅只是模拟，假设有一个无限货物的仓库货柜(id = 1)
        for (DeliveryOrderItemDO deliveryOrderItem : deliveryOrderItems) {
            deliveryOrderItem.setPickingCount(deliveryOrderItem.getSaleQuantity());
            deliveryOrderItem.setSkuContainerId("1");
        }
        return new ScheduleDeliveryDTO(deliveryOrder, deliveryOrderItems);
    }

    /**
     * 生成出库单id
     * TODO 接入发号器
     */
    private String genDeliveryOrderId() {
        return RandomHelper.getString(10, RandomHelper.DIST_NUMBER);
    }
}
