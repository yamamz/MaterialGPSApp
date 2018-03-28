package com.yamamz.materialgpsapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raymundo T. Melecio on 3/27/2018.
 */

public class MapObject {
   private ArrayList<LatLng> latLngList;
   private Double area;

    public MapObject(ArrayList<LatLng> latLngList,Double area) {
        this.latLngList = latLngList;
        this.area=area;
    }

    public ArrayList<LatLng> getLatLngList() {
        return latLngList;
    }

    public Double getArea() {
        return area;
    }
}
