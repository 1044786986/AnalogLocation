package com.example.ljh.analoglocaction;

import java.io.Serializable;

/**
 * Created by ljh on 2017/12/27.
 */

public class LocationQueryBean implements Serializable{
    private String name;
    private String latitude;
    private String longitude;
    private String address;

    public LocationQueryBean(String name,String address,String latitude,String longitude){
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
