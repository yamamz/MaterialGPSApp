package com.yamamz.materialgpsapp.model;

import io.realm.RealmObject;

/**
 * Created by Raymundo T. Melecio on 11/30/2016.
 */
public class LocationModel extends RealmObject {

    private Double elevation;
    private String  count;
    private Double latitude, longitude;




    public LocationModel() {

    }

    public LocationModel(Double latitude, Double longitude,  String count,Double elevation) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.count = count;
        this.elevation=elevation;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
}