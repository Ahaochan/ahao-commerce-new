package moe.ahao.commerce.product.application;

import moe.ahao.commerce.product.api.dto.ProductSkuDTO;
import moe.ahao.commerce.product.api.query.GetProductSkuQuery;
import moe.ahao.commerce.product.infrastructure.exception.ProductExceptionEnum;
import moe.ahao.commerce.product.infrastructure.repository.impl.mybatis.data.ProductSkuDO;
import moe.ahao.commerce.product.infrastructure.repository.impl.mybatis.mapper.ProductSkuMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetProductSkuQueryService {
    @Autowired
    private ProductSkuMapper productSkuMapper;

    public ProductSkuDTO query(GetProductSkuQuery query) {
        String skuCode = query.getSkuCode();
        if (StringUtils.isEmpty(skuCode)) {
            throw ProductExceptionEnum.SKU_CODE_IS_NULL.msg();
        }

        ProductSkuDO data = productSkuMapper.selectOneBySkuCode(skuCode);
        if (data == null) {
            return null;
        }

        ProductSkuDTO dto = new ProductSkuDTO();
        dto.setProductId(data.getProductId());
        dto.setProductType(data.getProductType());
        dto.setSkuCode(data.getSkuCode());
        dto.setProductName(data.getProductName());
        dto.setProductImg(data.getProductImg());
        dto.setProductUnit(data.getProductUnit());
        dto.setSalePrice(data.getSalePrice());
        dto.setPurchasePrice(data.getPurchasePrice());
        return dto;
    }
}
