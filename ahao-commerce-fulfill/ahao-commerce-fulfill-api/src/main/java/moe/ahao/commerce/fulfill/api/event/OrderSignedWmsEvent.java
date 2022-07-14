package moe.ahao.commerce.fulfill.api.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 订单已签收物流结果消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderSignedWmsEvent extends BaseWmsShipEvent {
    /**
     * 签收事件
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date signedTime;
}
