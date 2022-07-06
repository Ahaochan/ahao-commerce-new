package moe.ahao.commerce.address.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressFullDTO {

    /**
     * 省
     */
    private String provinceCode;
    private String province;

    /**
     * 市
     */
    private String cityCode;
    private String city;

    /**
     * 区
     */
    private String areaCode;
    private String area;

    /**
     * 街道
     */
    private String streetCode;
    private String street;

    public AddressFullDTO(String provinceCode, String province) {
        this.provinceCode = provinceCode;
        this.province = province;
    }

    public AddressFullDTO(String provinceCode, String province, String cityCode, String city) {
        this.provinceCode = provinceCode;
        this.province = province;
        this.cityCode = cityCode;
        this.city = city;
    }

    public AddressFullDTO(String provinceCode, String province, String cityCode, String city, String areaCode, String area) {
        this.provinceCode = provinceCode;
        this.province = province;
        this.cityCode = cityCode;
        this.city = city;
        this.areaCode = areaCode;
        this.area = area;
    }

    public AddressFullDTO(String provinceCode, String province, String cityCode, String city, String areaCode, String area, String streetCode, String street) {
        this.provinceCode = provinceCode;
        this.province = province;
        this.cityCode = cityCode;
        this.city = city;
        this.areaCode = areaCode;
        this.area = area;
        this.streetCode = streetCode;
        this.street = street;
    }
}
