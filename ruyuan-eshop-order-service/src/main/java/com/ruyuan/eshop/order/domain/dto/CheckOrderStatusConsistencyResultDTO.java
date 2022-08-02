package com.ruyuan.eshop.order.domain.dto;

import com.ruyuan.eshop.order.domain.entity.OrderInfoDO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 检查订单数据库与ES中一致性结果
 * @author zhonghuashishan
 * @version 1.0
 **/
@Data
@Builder
public class CheckOrderStatusConsistencyResultDTO {

    /**
     * 校验结果
     */
    private Boolean result;
    /**
     * 不同的订单的集合
     */
    private List<OrderInfoDO> diffOrderInfos;

}
