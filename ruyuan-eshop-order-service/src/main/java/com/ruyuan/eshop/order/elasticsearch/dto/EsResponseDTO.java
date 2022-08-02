package com.ruyuan.eshop.order.elasticsearch.dto;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * es查询结果
 *
 * @author zhonghuashishan
 * @version 1.0
 */
@Data
public class EsResponseDTO {

    private long total;

    private JSONArray data;
}
