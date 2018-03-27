package com.yamamz.materialgpsapp.plotMVP

import android.graphics.Color
import android.os.Environment
import android.support.design.widget.Snackbar
import com.google.android.gms.maps.model.LatLng
import com.yamamz.materialgpsapp.R.id.tvArea
import com.yamamz.materialgpsapp.utils.CoordinateConversion
import kotlinx.android.synthetic.main.activity_plot.*
import kotlinx.android.synthetic.main.content_plot.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*

/**
 * Created by Raymundo T. Melecio on 3/25/2018.
 */
class PlotInteractor(var presenter: PlotMVP.Presenter): PlotMVP.Interactor {
    override fun convertWGSToUTM(latLng: LatLng?) {
        val convertUtm = CoordinateConversion()
        val UTM = latLng?.latitude?.let { latLng.longitude.let { it1 -> convertUtm.latLon2UTM(it, it1) } }
        val lastdot = UTM?.lastIndexOf("-")
        val E = lastdot?.let { UTM.substring(0, it) }
        val N = UTM?.length?.let { lastdot?.plus(1)?.let { it1 -> UTM.substring(it1, it) } }
        var EastingFormat = java.lang.Double.parseDouble(E)
        var NorthingFormat = java.lang.Double.parseDouble(N)
        EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
        NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0

        presenter.addNorthingEasting(NorthingFormat,EastingFormat)
    }

    override fun generateCSV(filename:String,latLngList: ArrayList<LatLng>?) {
        try {
            val rand = Random()
            val intDate = rand.nextInt(Date().time.toInt())
            val root = File(Environment.getExternalStorageDirectory(), "CSV")
            if (!root.exists()) {
                root.mkdirs()
            }

            val csvfile = File(root,filename.toString().plus(intDate).plus("WGS_84.csv"))
            val writer = FileWriter(csvfile)
            //append the lat lon header to csv file
            writer.append("Latitude,Longitude \n")
            latLngList?.forEach{
                writer.append(it.latitude.toString().plus(",".plus(it.longitude).plus("\n")))
            }

            writer.flush()
            writer.close()
            presenter.showSnackBar("Saved and exported CSV to".plus(root.absolutePath))
            presenter.clearAllvalues()


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}