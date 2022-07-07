package moe.ahao.commerce.tms.application;

import moe.ahao.commerce.tms.api.command.CancelSendOutCommand;
import moe.ahao.commerce.tms.infrastructure.repository.impl.mybatis.mapper.LogisticOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelSendOutAppService {
    @Autowired
    private LogisticOrderMapper logisticOrderMapper;

    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelSendOut(CancelSendOutCommand command) {
        String orderId = command.getOrderId();
        // 1. 移除物流单
        logisticOrderMapper.deleteListByOrderId(orderId);
        return true;
    }
}
