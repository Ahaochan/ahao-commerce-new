package com.ruyuan.eshop.order.converter;

import com.ruyuan.eshop.customer.domain.request.CustomerReviewReturnGoodsRequest;
import com.ruyuan.eshop.order.domain.dto.*;
import com.ruyuan.eshop.order.domain.entity.*;
import com.ruyuan.eshop.order.domain.query.AfterSaleQuery;
import com.ruyuan.eshop.order.domain.request.CustomerAuditAssembleRequest;
import com.ruyuan.eshop.order.domain.request.ManualAfterSaleDTO;
import com.ruyuan.eshop.order.domain.request.ReturnGoodsOrderRequest;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @author zhonghuashishan
 * @version 1.0
 */
@Mapper(componentModel = "spring")
public interface AfterSaleConverter {

    /**
     * 对象转换
     *
     * @param afterSaleInfoDO 对象
     * @return 对象
     */
    AfterSaleInfoDTO afterSaleInfoDO2DTO(AfterSaleInfoDO afterSaleInfoDO);

    /**
     * 对象转换
     *
     * @param afterSaleItemDO 对象
     * @return 对象
     */
    AfterSaleItemDTO afterSaleItemDO2DTO(AfterSaleItemDO afterSaleItemDO);

    /**
     * 对象转换
     *
     * @param afterSaleItemDOs 对象
     * @return 对象
     */
    List<AfterSaleItemDTO> afterSaleItemDO2DTO(List<AfterSaleItemDO> afterSaleItemDOs);


    /**
     * 对象转换
     *
     * @param afterSaleRefund 对象
     * @return 对象
     */
    AfterSaleRefundDTO afterSaleRefundDO2DTO(AfterSaleRefundDO afterSaleRefund);

    /**
     * 对象转换
     *
     * @param afterSaleRefunds 对象
     * @return 对象
     */
    List<AfterSaleRefundDTO> afterSaleRefundDO2DTO(List<AfterSaleRefundDO> afterSaleRefunds);


    /**
     * 对象转换
     *
     * @param afterSaleLog 对象
     * @return 对象
     */
    AfterSaleLogDTO afterSaleLogDO2DTO(AfterSaleLogDO afterSaleLog);

    /**
     * 对象转换
     *
     * @param afterSaleLogs 对象
     * @return 对象
     */
    List<AfterSaleLogDTO> afterSaleLogDO2DTO(List<AfterSaleLogDO> afterSaleLogs);

    /**
     * 对象转换
     *
     * @param query 对象
     * @return 对象
     */
    AfterSaleListQueryDTO afterSaleListQueryDTO(AfterSaleQuery query);

    /**
     * 对象转换
     *
     * @param customerReviewReturnGoodsRequest 对象
     * @return 对象
     */
    CustomerAuditAssembleRequest review2AuditPass(CustomerReviewReturnGoodsRequest customerReviewReturnGoodsRequest);

    /**
     * 对象转换
     *
     * @param returnGoodsOrderRequest 对象
     * @return 对象
     */
    ManualAfterSaleDTO returnGoodRequest2AssembleRequest(ReturnGoodsOrderRequest returnGoodsOrderRequest);

    /**
     * 对象转换
     *
     * @param orderItemDO 对象
     * @return 对象
     */
    AfterSaleItemDTO orderItemDO2AfterSaleItemDTO(OrderItemDO orderItemDO);

    /**
     * 对象转换
     *
     * @param afterSaleInfoDTO 对象
     * @return 对象
     */
    AfterSaleInfoDO afterSaleInfoDTO2DO(AfterSaleInfoDTO afterSaleInfoDTO);
}
