package com.yamamz.materialgpsapp.plotMVP

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions

/**
 * Created by Raymundo T. Melecio on 3/25/2018.
 */
class PlotMVP {

    interface View{
        fun putMarkerOnMap(p0: LatLng?)
        fun createRecOption(p0: LatLng?):PolygonOptions
        fun  animateMapFlyGotoLoc(latLng: LatLng?)

    }

    interface Presenter{
        fun drawShape(p0:LatLng?)
        fun animateMapFlyGotoLoc(latLng: LatLng?)

    }

    interface Interactor{


    }

}
