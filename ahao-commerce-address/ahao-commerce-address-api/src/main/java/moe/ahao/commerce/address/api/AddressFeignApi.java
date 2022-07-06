package moe.ahao.commerce.address.api;

import moe.ahao.commerce.address.api.dto.AddressDTO;
import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.domain.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AddressFeignApi extends AddressApi {
    String CONTEXT = "/api/address/";

    @PostMapping("/listProvinces")
    Result<List<AddressDTO>> listProvinces();
    @PostMapping("/queryAddress")
    Result<AddressFullDTO> queryFullAddress(@RequestBody AddressQuery query);
}
