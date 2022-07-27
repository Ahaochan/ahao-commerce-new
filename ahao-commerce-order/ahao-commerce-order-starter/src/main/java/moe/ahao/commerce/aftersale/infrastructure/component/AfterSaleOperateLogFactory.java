package moe.ahao.commerce.aftersale.infrastructure.component;

import moe.ahao.commerce.aftersale.infrastructure.enums.AfterSaleStatusChangeEnum;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleInfoDO;
import moe.ahao.commerce.aftersale.infrastructure.repository.impl.mybatis.data.AfterSaleLogDO;
import org.springframework.stereotype.Component;

/**
 * 售后单操作日志工厂
 */
@Component
public class AfterSaleOperateLogFactory {
    /**
     * 获取售后操作日志
     */
    public AfterSaleLogDO get(AfterSaleInfoDO afterSaleInfo, AfterSaleStatusChangeEnum statusChange) {
        String operateRemark = statusChange.getOperateRemark();
        int preStatus = statusChange.getPreStatus().getCode();
        int currentStatus = statusChange.getCurrentStatus().getCode();
        return create(afterSaleInfo, preStatus, currentStatus, operateRemark);
    }

    /**
     * 创建售后单操作日志
     */
    private AfterSaleLogDO create(AfterSaleInfoDO afterSaleInfo, int preStatus, int currentStatus, String operateRemark) {
        AfterSaleLogDO log = new AfterSaleLogDO();

        log.setAfterSaleId(String.valueOf(afterSaleInfo.getAfterSaleId()));
        log.setPreStatus(preStatus);
        log.setCurrentStatus(currentStatus);
        log.setRemark(operateRemark);

        return log;
    }


}
