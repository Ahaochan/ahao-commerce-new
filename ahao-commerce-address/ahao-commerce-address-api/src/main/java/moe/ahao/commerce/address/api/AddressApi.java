package moe.ahao.commerce.address.api;


import moe.ahao.commerce.address.api.dto.AddressDTO;
import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.domain.entity.Result;

import java.util.List;

/**
 * 地址服务业务接口
 */
/* package */  interface AddressApi {
    Result<List<AddressDTO>> listProvinces();
    Result<AddressFullDTO> queryFullAddress(AddressQuery query);
}
