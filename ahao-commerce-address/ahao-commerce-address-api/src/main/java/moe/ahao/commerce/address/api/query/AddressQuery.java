package moe.ahao.commerce.address.api.query;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行政地址查询条件
 */
@Data
@NoArgsConstructor
public class AddressQuery {
    /**
     * 省
     */
    private String provinceCode;
    private String province;

    public AddressQuery(String provinceCode, String province) {
        this(provinceCode, province, null, null, null, null, null, null);
    }

    /**
     * 市
     */
    private String cityCode;
    private String city;

    public AddressQuery(String provinceCode, String province, String cityCode, String city) {
        this(provinceCode, province, cityCode, city, null, null, null, null);
    }

    /**
     * 区
     */
    private String areaCode;
    private String area;

    public AddressQuery(String provinceCode, String province, String cityCode, String city, String areaCode, String area) {
        this(provinceCode, province, cityCode, city, areaCode, area, null, null);
    }

    /**
     * 街道
     */
    private String streetCode;
    private String street;

    public AddressQuery(String provinceCode, String province, String cityCode, String city, String areaCode, String area, String streetCode, String street) {
        this.provinceCode = provinceCode;
        this.province = province;
        this.cityCode = cityCode;
        this.city = city;
        this.areaCode = areaCode;
        this.area = area;
        this.streetCode = streetCode;
        this.street = street;
    }

    public AddressQuery(AddressQuery that) {
        this.provinceCode = that.provinceCode;
        this.province = that.province;
        this.cityCode = that.cityCode;
        this.city = that.city;
        this.areaCode = that.areaCode;
        this.area = that.area;
        this.streetCode = that.streetCode;
        this.street = that.street;
    }
}
