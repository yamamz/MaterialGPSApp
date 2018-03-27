package com.yamamz.materialgpsapp.plotMVP

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.yamamz.materialgpsapp.model.MapObject

/**
 * Created by Raymundo T. Melecio on 3/25/2018.
 */
class PlotMVP {

    interface View{
        fun putMarkerOnMap(p0: LatLng?)
        fun createRecOption(p0: LatLng?):PolygonOptions
        fun  animateMapFlyGotoLoc(latLng: LatLng?)
        fun showSnackbar(msg: String)
        fun clearAllvalues()
        fun addNorthingEasting(northingFormat: Double, eastingFormat: Double)

    }

    interface Presenter{
        fun drawShape(p0:LatLng?)
        fun animateMapFlyGotoLoc(latLng: LatLng?)
        fun generateCSV(filename: String,latLngList: ArrayList<LatLng>?)
        fun showSnackBar(msg:String)
        fun clearAllvalues()
        fun convertWGSToUTM(latLng: LatLng?)
        fun addNorthingEasting(northingFormat: Double, eastingFormat: Double)


    }

    interface Interactor{
        fun generateCSV(filename:String,latLngList: ArrayList<LatLng>?)
        fun convertWGSToUTM(latLng: LatLng?)


    }

}
