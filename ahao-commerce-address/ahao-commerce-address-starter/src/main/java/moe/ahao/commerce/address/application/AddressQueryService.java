package moe.ahao.commerce.address.application;

import lombok.extern.slf4j.Slf4j;
import moe.ahao.commerce.address.api.dto.AddressDTO;
import moe.ahao.commerce.address.api.dto.AddressFullDTO;
import moe.ahao.commerce.address.api.query.AddressQuery;
import moe.ahao.commerce.address.infrastructure.exception.AddressExceptionEnum;
import moe.ahao.commerce.address.infrastructure.repository.impl.mybatis.data.AddressDO;
import moe.ahao.commerce.address.infrastructure.repository.impl.mybatis.mapper.AddressMapper;
import moe.ahao.exception.CommonBizExceptionEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AddressQueryService {
    @Autowired
    private AddressMapper addressMapper;

    public List<AddressDTO> listProvince() {
        // 1. 查询出省地质信息
        List<AddressDO> provinces = addressMapper.selectListByParentCode(AddressDO.PROVINCE_PARENT_CODE);
        // 2. 根据sort字段排序
        provinces.sort(Comparator.comparing(AddressDO::getSort));

        // 3. 封装响应体
        List<AddressDTO> dataList = new ArrayList<>();
        for (AddressDO province : provinces) {
            AddressDTO data = new AddressDTO();
            data.setCode(province.getCode());
            data.setName(province.getName());
            data.setShortName(province.getShortName());
            data.setParentCode(province.getParentCode());
            data.setLng(province.getLng());
            data.setLat(province.getLat());
            data.setSort(province.getSort());

            dataList.add(data);
        }
        return dataList;
    }

    public AddressFullDTO queryFullAddress(AddressQuery query) {
        // 1. 参数校验
        String queryStreet = query.getStreet();
        String queryStreetCode = query.getStreetCode();
        String queryArea = query.getArea();
        String queryAreaCode = query.getAreaCode();
        String queryCity = query.getCity();
        String queryCityCode = query.getCityCode();
        String queryProvince = query.getProvince();
        String queryProvinceCode = query.getProvinceCode();
        if (StringUtils.isAllBlank(queryProvince, queryProvinceCode, queryCity, queryCityCode, queryArea, queryAreaCode, queryStreet, queryStreetCode)) {
            throw CommonBizExceptionEnum.SERVER_ILLEGAL_ARGUMENT_ERROR.msg();
        }

        // 2. 查询信息
        AddressDTO street = this.queryFullAddress(queryStreetCode, queryStreet, null);
        AddressDTO area = this.queryFullAddress(queryAreaCode, queryArea, street);
        AddressDTO city = this.queryFullAddress(queryCityCode, queryCity, area);
        AddressDTO province = this.queryFullAddress(queryProvinceCode, queryProvince, city);
        if(street == null && area == null && city == null && province == null) {
            return null;
        }

        // 5、组装候选结果集合
        AddressFullDTO addressFullDTO = new AddressFullDTO();
        if(street != null) {
            addressFullDTO.setStreet(street.getName());
            addressFullDTO.setStreetCode(street.getCode());
        }
        if(area != null) {
            addressFullDTO.setArea(area.getName());
            addressFullDTO.setAreaCode(area.getCode());
        }
        if(city != null) {
            addressFullDTO.setCity(city.getName());
            addressFullDTO.setCityCode(city.getCode());
        }
        if(province != null) {
            addressFullDTO.setProvince(province.getName());
            addressFullDTO.setProvinceCode(province.getCode());
        }
        return addressFullDTO;
    }

    private AddressDTO queryFullAddress(String code, String name, AddressDTO subAddress) {
        boolean nameBlank = StringUtils.isBlank(name);
        boolean codeBlank = StringUtils.isBlank(code);
        boolean subAddressEmpty = subAddress == null || StringUtils.isBlank(subAddress.getParentCode());
        if(nameBlank && codeBlank && subAddressEmpty) {
            return null;
        }

        Set<String> codes = new HashSet<>();
        if(!codeBlank) {
            codes.add(code);
        }
        if(!subAddressEmpty) {
            codes.add(subAddress.getParentCode());
        }
        List<AddressDO> result = addressMapper.selectListByCodesOrName(codes, name);
        if(CollectionUtils.isEmpty(result)) {
            throw AddressExceptionEnum.ADDRESS_NOT_FOUND.msg(name, codes);
        }
        int size = result.size();
        if(size > 1) {
            throw AddressExceptionEnum.ADDRESS_MULTI_RESULT.msg(name, codes);
        }
        AddressDO data = result.get(0);
        if(!nameBlank && !Objects.equals(name, data.getName())) {
            throw AddressExceptionEnum.ADDRESS_NOT_MATCH.msg(name, codes, data.getName(), data.getCode());
        }
        if(!codeBlank && !Objects.equals(code, data.getCode())) {
            throw AddressExceptionEnum.ADDRESS_NOT_MATCH.msg(name, codes, data.getName(), data.getCode());
        }

        AddressDTO dto = new AddressDTO();
        dto.setCode(data.getCode());
        dto.setName(data.getName());
        dto.setShortName(data.getShortName());
        dto.setParentCode(data.getParentCode());
        dto.setLng(data.getLng());
        dto.setLat(data.getLat());
        dto.setSort(data.getSort());
        return dto;
    }
}
