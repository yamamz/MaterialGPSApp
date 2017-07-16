package com.yamamz.materialgpsapp.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by AMRI on 1/12/2017.
 */

public class SaveLocation extends RealmObject{
    @PrimaryKey
    private String fileName;
    private RealmList<LocationModel> locationModelList;
    private   Double area;
    private Double elevation;

    public SaveLocation() {

    }

    public SaveLocation(String fileName, RealmList <LocationModel> locationModels, Double area) {
        this.fileName = fileName;
        this.locationModelList =locationModels;
        this.area = area;

    }

    public SaveLocation(String fileName,Double area){
        this.fileName=fileName;
        this.area=area;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public RealmList<LocationModel> getLocations() {
        return locationModelList;
    }

    public void setLocations(RealmList<LocationModel>locations) {
        this.locationModelList = locations;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }
}
