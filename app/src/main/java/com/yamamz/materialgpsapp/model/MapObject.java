package com.yamamz.materialgpsapp.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Raymundo T. Melecio on 3/27/2018.
 */

public class MapObject {
   private ArrayList<LatLng> latLngList= new ArrayList<>();


    public MapObject(ArrayList<LatLng> latLngList) {
        this.latLngList = latLngList;
    }

    public ArrayList<LatLng> getLatLngList() {
        return latLngList;
    }
}
