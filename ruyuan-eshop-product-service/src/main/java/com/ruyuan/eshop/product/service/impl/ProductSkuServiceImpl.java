package com.ruyuan.eshop.product.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ruyuan.eshop.common.utils.ExtJsonUtil;
import com.ruyuan.eshop.common.utils.ParamCheckUtil;
import com.ruyuan.eshop.product.converter.ProductConverter;
import com.ruyuan.eshop.product.dao.ProductSkuDAO;
import com.ruyuan.eshop.product.domain.dto.PreSaleInfoDTO;
import com.ruyuan.eshop.product.domain.dto.ProductSkuDTO;
import com.ruyuan.eshop.product.domain.entity.ProductSkuDO;
import com.ruyuan.eshop.product.enums.ProductTypeEnum;
import com.ruyuan.eshop.product.exception.ProductErrorCodeEnum;
import com.ruyuan.eshop.product.service.ProductSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Service
public class ProductSkuServiceImpl implements ProductSkuService {

    @Autowired
    private ProductSkuDAO productSkuDAO;

    @Autowired
    private ProductConverter productConverter;

    @Override
    public ProductSkuDTO getProductSkuByCode(String skuCode) {
        ParamCheckUtil.checkStringNonEmpty(skuCode, ProductErrorCodeEnum.SKU_CODE_IS_NULL);

        ProductSkuDO productSkuDO = productSkuDAO.getProductSkuByCode(skuCode);
        if (productSkuDO == null) {
            return null;
        }
        ProductSkuDTO productSkuDTO = productConverter.convert(productSkuDO);

        //设置预售商品信息
        setPreSaleInfo(productSkuDTO, productSkuDO.getExtJson());

        return productSkuDTO;
    }

    @Override
    public List<ProductSkuDTO> listProductSkuByCode(List<String> skuCodeList) {
        ParamCheckUtil.checkCollectionNonEmpty(skuCodeList, ProductErrorCodeEnum.SKU_CODE_IS_NULL);
        List<ProductSkuDO> productSkuDOList = productSkuDAO.listProductSkuByCode(skuCodeList);


        List<ProductSkuDTO> productSkuDTOList = productConverter.convert(productSkuDOList);

        //设置预售商品信息
        if (CollectionUtils.isNotEmpty(productSkuDTOList)) {
            Map<String, ProductSkuDO> map = productSkuDOList.stream()
                    .collect(Collectors.toMap(ProductSkuDO::getSkuCode, productSkuDO -> productSkuDO));
            productSkuDTOList.forEach(dto -> {
                setPreSaleInfo(dto, map.get(dto.getSkuCode()).getExtJson());
            });
        }

        return productSkuDTOList;
    }

    /**
     * 设置预售商品信息
     *
     * @param productSkuDTO
     * @param extJson
     */
    private void setPreSaleInfo(ProductSkuDTO productSkuDTO, String extJson) {
        if (!ProductTypeEnum.PRE_SALE.getCode().equals(productSkuDTO.getProductType())) {
            return;
        }
        ParamCheckUtil.checkStringNonEmpty(extJson, ProductErrorCodeEnum.PRE_SALE_INFO_IS_NULL);
        productSkuDTO.setPreSaleInfo(ExtJsonUtil.parseExtJson(extJson, PreSaleInfoDTO.class));
    }
}