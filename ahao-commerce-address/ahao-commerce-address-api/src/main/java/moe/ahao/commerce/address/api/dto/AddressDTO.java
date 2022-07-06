package moe.ahao.commerce.address.api.dto;

import lombok.Data;

@Data
public class AddressDTO {
    /**
     * 代码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 简称
     */
    private String shortName;
    /**
     * 父级代码
     */
    private String parentCode;

    /**
     * 经度
     */
    private String lng;
    /**
     * 纬度
     */
    private String lat;
    /**
     * 排序
     */
    private Integer sort;
}
