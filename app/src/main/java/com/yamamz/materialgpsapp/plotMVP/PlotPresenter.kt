package com.yamamz.materialgpsapp.plotMVP

import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng

/**
 * Created by Raymundo T. Melecio on 3/25/2018.
 */
class PlotPresenter(var view: PlotMVP.View): PlotMVP.Presenter {
    override fun animateMapFlyGotoLoc(latLng: LatLng?) {
        view.animateMapFlyGotoLoc(latLng)
    }

    override fun drawShape(p0: LatLng?) {

       view.putMarkerOnMap(p0)
    }


}