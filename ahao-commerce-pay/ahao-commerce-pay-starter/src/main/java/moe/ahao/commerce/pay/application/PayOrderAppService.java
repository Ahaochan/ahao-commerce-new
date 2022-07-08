package moe.ahao.commerce.pay.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.common.enums.PayTypeEnum;
import moe.ahao.commerce.pay.api.command.PayOrderCommand;
import moe.ahao.commerce.pay.api.dto.PayOrderDTO;
import moe.ahao.util.commons.lang.RandomHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PayOrderAppService {

    public PayOrderDTO pay(PayOrderCommand command) {
        String orderId = command.getOrderId();
        String outTradeNo = RandomHelper.getString(19, RandomHelper.DIST_NUMBER);

        PayOrderDTO payOrderDTO = new PayOrderDTO();
        payOrderDTO.setOrderId(orderId);
        payOrderDTO.setOutTradeNo(outTradeNo);
        payOrderDTO.setPayType(PayTypeEnum.WEIXIN_PAY.getCode());
        Map<String, Object> payData = this.getThirdPayData(command);
        payOrderDTO.setPayData(payData);

        return payOrderDTO;
    }

    private Map<String, Object> getThirdPayData(PayOrderCommand command) {
        // 模拟调用了第三方支付平台的支付接口
        Map<String, Object> payData = new HashMap<>();
        payData.put("appid", "wx207d34495e688e0c");
        payData.put("prepayId", RandomHelper.getString(11, RandomHelper.DIST_NUMBER));
        payData.put("payAmount", command.getPayAmount());
        payData.put("webUrl", "http://xxx/payurl");
        return payData;
    }
}
