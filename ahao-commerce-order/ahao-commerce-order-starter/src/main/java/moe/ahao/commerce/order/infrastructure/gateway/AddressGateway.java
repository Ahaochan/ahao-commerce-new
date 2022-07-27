package moe.ahao.commerce.order.infrastructure.gateway;

import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.commerce.order.infrastructure.gateway.feign.AddressFeignClient;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 地址服务远程接口
 */
@Component
public class AddressGateway {
    /**
     * 地址服务
     */
    @Autowired
    private AddressFeignClient addressFeignClient;

    /**
     * 查询行政地址
     *
     * @param query
     * @return
     */
    public AddressFullDTO queryAddress(AddressQuery query) {
        Result<AddressFullDTO> result = addressFeignClient.queryFullAddress(query);
        if (result.getCode() == Result.SUCCESS && result.getObj() != null) {
            return result.getObj();
        }
        return null;
    }

}
