package moe.ahao.commerce.order.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.order.api.command.GenOrderIdCommand;
import moe.ahao.commerce.order.infrastructure.enums.BusinessIdentifierEnum;
import moe.ahao.commerce.order.infrastructure.enums.OrderIdTypeEnum;
import moe.ahao.commerce.order.infrastructure.exception.OrderExceptionEnum;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.data.OrderAutoNoDO;
import moe.ahao.commerce.order.infrastructure.repository.impl.mybatis.mapper.OrderAutoNoMapper;
import moe.ahao.commerce.order.infrastructure.utils.ObfuscateNumberHelper;
import moe.ahao.util.commons.lang.time.DateHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 订单号生成AppService
 */
@Slf4j
@Service
public class GenOrderIdAppService {
    @Autowired
    private OrderAutoNoMapper orderAutoNoMapper;

    /**
     * 生成订单号
     * 19位，2位是业务类型，比如10开头是正向，20开头是逆向，然后中间6位是日期，然后中间8位是序列号，最后3位是用户ID后三位
     * 用户ID不足3位前面补0
     */
    public String generate(GenOrderIdCommand command) {
        this.check(command);

        Integer businessIdentifier = command.getBusinessIdentifier();
        String userId = command.getUserId();
        Integer orderIdType = command.getOrderIdType();

        String part1 = String.valueOf(orderIdType);
        String part2 = this.getDateTimeKey();
        String part3 = this.getAutoNoKey();
        String part4 = this.getUserIdKey(userId);
        String orderId = part1 + part2 + part3 + part4;

        return orderId;
    }

    private void check(GenOrderIdCommand command) {
        Integer businessIdentifier = command.getBusinessIdentifier();
        if (businessIdentifier == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_IS_NULL.msg();
        }
        BusinessIdentifierEnum businessIdentifierEnum = BusinessIdentifierEnum.getByCode(businessIdentifier);
        if (businessIdentifierEnum == null) {
            throw OrderExceptionEnum.BUSINESS_IDENTIFIER_ERROR.msg();
        }
        String userId = command.getUserId();
        if (StringUtils.isEmpty(userId)) {
            throw OrderExceptionEnum.USER_ID_IS_NULL.msg();
        }
        Integer orderIdType = command.getOrderIdType();
        OrderIdTypeEnum orderNoTypeEnum = OrderIdTypeEnum.getByCode(orderIdType);
        if (orderNoTypeEnum == null) {
            throw OrderExceptionEnum.ORDER_NO_TYPE_ERROR.msg();
        }
    }

    /**
     * 生成订单号的中间6位日期
     */
    private String getDateTimeKey() {
        return DateHelper.getString(new Date(), "yyMMdd");
    }

    /**
     * 生成订单号中间的8位序列号
     */
    private String getAutoNoKey() {
        // 1. 从数据库取号
        OrderAutoNoDO orderAutoNoDO = new OrderAutoNoDO();
        orderAutoNoMapper.insert(orderAutoNoDO);
        Long autoNo = orderAutoNoDO.getId();

        // 2. 补零
        long obfuscateNumber = ObfuscateNumberHelper.genNo(autoNo, 8);
        String orderId = String.valueOf(obfuscateNumber);
        return orderId;
    }

    /**
     * 截取用户ID的后三位
     */
    private String getUserIdKey(String userId) {
        // 1. 如果userId的长度大于或等于3，则直接返回
        if (userId.length() >= 3) {
            return userId.substring(userId.length() - 3);
        } else if (userId.length() == 2) {
            // 2. 如果userId的长度小于3，则直接前面补0
            return "0" + userId;
        } else if (userId.length() == 1) {
            return "00" + userId;
        } else {
            throw OrderExceptionEnum.USER_ID_IS_NULL.msg();
        }
    }
}
