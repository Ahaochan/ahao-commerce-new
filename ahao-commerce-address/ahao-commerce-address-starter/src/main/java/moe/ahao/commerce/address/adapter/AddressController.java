package moe.ahao.commerce.address.adapter;

import moe.ahao.commerce.address.api.AddressFeignApi;
import moe.ahao.commerce.address.api.dto.AddressDTO;
import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.commerce.address.application.AddressQueryService;
import moe.ahao.domain.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AddressFeignApi.PATH)
public class AddressController implements AddressFeignApi {

    @Autowired
    private AddressQueryService addressQueryService;

    @Override
    public Result<List<AddressDTO>> listProvinces() {
        List<AddressDTO> list = addressQueryService.listProvince();
        return Result.success(list);
    }

    @Override
    public Result<AddressFullDTO> queryFullAddress(@RequestBody AddressQuery query) {
        AddressFullDTO dto = addressQueryService.queryFullAddress(query);
        if(dto == null) {
            return Result.failure();
        } else {
            return Result.success(dto);
        }
    }
}
