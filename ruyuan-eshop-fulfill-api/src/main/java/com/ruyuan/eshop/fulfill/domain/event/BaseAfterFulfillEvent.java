package com.ruyuan.eshop.fulfill.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单履约后事件基类
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseAfterFulfillEvent implements Serializable {

    private static final long serialVersionUID = 3310845067561671265L;

    /**
     * 订单编号
     */
    private String orderId;
}
