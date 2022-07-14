package com.ruyuan.eshop.order.remote;

import moe.ahao.commerce.address.api.AddressFeignApi;
import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 地址服务远程接口
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Component
public class AddressRemote {

    /**
     * 地址服务
     */
    @Autowired
    private AddressFeignApi addressFeignApi;

    /**
     * 查询行政地址
     *
     * @param query
     * @return
     */
    public AddressFullDTO queryAddress(AddressQuery query) {
        Result<AddressFullDTO> result = addressFeignApi.queryFullAddress(query);
        if (result.getCode() == Result.SUCCESS && result.getObj() != null) {
            return result.getObj();
        }
        return null;
    }

}
