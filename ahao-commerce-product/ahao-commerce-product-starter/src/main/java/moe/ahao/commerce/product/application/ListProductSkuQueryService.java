package moe.ahao.commerce.product.application;

import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.ListProductSkuQuery;
import moe.ahao.commerce.product.infrastructure.exception.ProductExceptionEnum;
import moe.ahao.commerce.product.infrastructure.repository.impl.mybatis.data.ProductSkuDO;
import moe.ahao.commerce.product.infrastructure.repository.impl.mybatis.mapper.ProductSkuMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ListProductSkuQueryService {
    @Autowired
    private ProductSkuMapper productSkuMapper;

    public List<ProductSkuDTO> query(ListProductSkuQuery query) {
        List<String> skuCodeList = query.getSkuCodeList();
        if (CollectionUtils.isEmpty(skuCodeList)) {
            throw ProductExceptionEnum.SKU_CODE_IS_NULL.msg();
        }

        List<ProductSkuDO> dataList = productSkuMapper.selectListBySkuCodeList(skuCodeList);

        List<ProductSkuDTO> dtoList = new ArrayList<>();
        for (ProductSkuDO data : dataList) {
            ProductSkuDTO dto = new ProductSkuDTO();
            dto.setProductId(data.getProductId());
            dto.setProductType(data.getProductType());
            dto.setSkuCode(data.getSkuCode());
            dto.setProductName(data.getProductName());
            dto.setProductImg(data.getProductImg());
            dto.setProductUnit(data.getProductUnit());
            dto.setSalePrice(data.getSalePrice());
            dto.setPurchasePrice(data.getPurchasePrice());

            dtoList.add(dto);
        }
        return dtoList;
    }
}
