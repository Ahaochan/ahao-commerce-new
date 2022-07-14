package moe.ahao.commerce.fulfill.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.fulfill.api.command.CancelFulfillCommand;
import moe.ahao.commerce.fulfill.infrastructure.gateway.TmsGateway;
import moe.ahao.commerce.fulfill.infrastructure.gateway.WmsGateway;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.data.OrderFulfillDO;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper.OrderFulfillItemMapper;
import moe.ahao.commerce.fulfill.infrastructure.repository.impl.mybatis.mapper.OrderFulfillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CancelFulfillAppService {
    @Autowired
    private OrderFulfillMapper orderFulfillMapper;
    @Autowired
    private OrderFulfillItemMapper orderFulfillItemMapper;

    @Autowired
    private WmsGateway wmsGateway;
    @Autowired
    private TmsGateway tmsGateway;

    public boolean cancelFulfillAndWmsAndTms(CancelFulfillCommand command) {
        // 1. 取消履约单
        this.cancelFulfill(command.getOrderId());

        // 2. 取消捡货
        wmsGateway.cancelPickGoods(command.getOrderId());

        // 3. 取消发货
        tmsGateway.cancelSendOut(command.getOrderId());

        return true;
    }

    public void cancelFulfill(String orderId) {
        // 1. 查询履约单
        OrderFulfillDO orderFulfill = orderFulfillMapper.selectOneByOrderId(orderId);
        if(orderFulfill == null) {
            return;
        }

        // 2. 删除履约单
        orderFulfillMapper.deleteById(orderFulfill.getId());

        // 3. 删除履约单条目
        orderFulfillItemMapper.deleteByFulfillId(orderFulfill.getFulfillId());
    }
}
