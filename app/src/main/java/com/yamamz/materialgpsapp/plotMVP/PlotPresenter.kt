package com.yamamz.materialgpsapp.plotMVP

import com.google.android.gms.maps.model.LatLng

/**
 * Created by Raymundo T. Melecio on 3/25/2018.
 */
class PlotPresenter(var view: PlotMVP.View): PlotMVP.Presenter {
    override fun addNorthingEasting(northingFormat: Double, eastingFormat: Double) {
        view.addNorthingEasting(northingFormat, eastingFormat)
    }

    override fun convertWGSToUTM(latLng: LatLng?) {
        interactor.convertWGSToUTM(latLng)
    }

    override fun clearAllvalues() {
        view.clearAllvalues()
    }

    override fun showSnackBar(msg:String) {
        view.showSnackbar(msg)
    }

    val interactor= PlotInteractor(this)
    override fun generateCSV(filename: String, latLngList: ArrayList<LatLng>?) {
interactor.generateCSV(filename,latLngList)
    }


    override fun animateMapFlyGotoLoc(latLng: LatLng?) {
        view.animateMapFlyGotoLoc(latLng)
    }

    override fun drawShape(p0: LatLng?) {

       view.putMarkerOnMap(p0)
    }


}