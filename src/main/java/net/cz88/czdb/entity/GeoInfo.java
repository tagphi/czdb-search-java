package net.cz88.czdb.entity;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class GeoInfo {
    private String code;
    private String country;
    private String countryCode;
    private String province;
    private String provinceCode;
    private String city;
    private String cityCode;
    private String district;
    private String districtCode;
    private String lat;
    private String lng;
    private String zone;
    private String continent;
    private String iana;
    private String ianaEn;

    private GeoInfo(Builder builder) {
        this.code = builder.code;
        this.country = builder.country;
        this.countryCode = builder.countryCode;
        this.province = builder.province;
        this.provinceCode = builder.provinceCode;
        this.city = builder.city;
        this.cityCode = builder.cityCode;
        this.district = builder.district;
        this.districtCode = builder.districtCode;
        this.lat = builder.lat;
        this.lng = builder.lng;
        this.zone = builder.zone;
        this.continent = builder.continent;
        this.iana = builder.iana;
        this.ianaEn = builder.ianaEn;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getIana() {
        return iana;
    }

    public void setIana(String iana) {
        this.iana = iana;
    }

    public String getIanaEn() {
        return ianaEn;
    }

    public void setIanaEn(String ianaEn) {
        this.ianaEn = ianaEn;
    }

    public static class Builder {
        private String code;
        private String country;
        private String countryCode;
        private String province;
        private String provinceCode;
        private String city;
        private String cityCode;
        private String district;
        private String districtCode;
        private String lat;
        private String lng;
        private String zone;
        private String continent;
        private String iana;
        private String ianaEn;

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder countryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public Builder province(String province) {
            this.province = province;
            return this;
        }

        public Builder provinceCode(String provinceCode) {
            this.provinceCode = provinceCode;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder cityCode(String cityCode) {
            this.cityCode = cityCode;
            return this;
        }

        public Builder district(String district) {
            this.district = district;
            return this;
        }

        public Builder districtCode(String districtCode) {
            this.districtCode = districtCode;
            return this;
        }

        public Builder lat(String lat) {
            this.lat = lat;
            return this;
        }

        public Builder lng(String lng) {
            this.lng = lng;
            return this;
        }

        public Builder zone(String zone) {
            this.zone = zone;
            return this;
        }

        public Builder continent(String continent) {
            this.continent = continent;
            return this;
        }

        public Builder iana(String iana) {
            this.iana = iana;
            return this;
        }

        public Builder ianaEn(String ianaEn) {
            this.ianaEn = ianaEn;
            return this;
        }

        public GeoInfo build() {
            return new GeoInfo(this);
        }
    }

    public static GeoInfo fromBytes(byte[] bytes) throws IOException {
        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes)) {
            return new Builder()
                    .code(unpacker.unpackString())
                    .country(unpacker.unpackString())
                    .countryCode(unpacker.unpackString())
                    .province(unpacker.unpackString())
                    .provinceCode(unpacker.unpackString())
                    .city(unpacker.unpackString())
                    .cityCode(unpacker.unpackString())
                    .district(unpacker.unpackString())
                    .districtCode(unpacker.unpackString())
                    .lat(unpacker.unpackString())
                    .lng(unpacker.unpackString())
                    .zone(unpacker.unpackString())
                    .continent(unpacker.unpackString())
                    .iana(unpacker.unpackString())
                    .ianaEn(unpacker.unpackString())
                    .build();
        }
    }
}