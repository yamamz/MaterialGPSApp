package com.yamamz.materialgpsapp.model;

/**
 * Created by AMRI on 7/14/2017.
 */

public class LatLong {

    Double lat,lng;

    public LatLong(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
