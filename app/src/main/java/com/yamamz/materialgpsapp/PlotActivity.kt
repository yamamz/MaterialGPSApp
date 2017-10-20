package com.yamamz.materialgpsapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.yamamz.materialgpsapp.utils.CoordinateConversion

import kotlinx.android.synthetic.main.activity_plot.*
import kotlinx.android.synthetic.main.content_plot.*
import java.text.DecimalFormat
import java.util.ArrayList

class PlotActivity : AppCompatActivity(), OnMapReadyCallback {

    var mMap:GoogleMap?=null
    private var polygon: Polygon?=null
    val hashMapMarker = HashMap<Int, Marker>()
    private var drawing: Boolean=false
    private var points: ArrayList<LatLng>?= ArrayList()
    private var  marker: Marker?=null
    override fun onMapReady(p0: GoogleMap?) {

        mMap = p0
        //move the camera to the center of the map before going to the target location
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))
        mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID//set hybrid map tile



        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
        val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        val shape = ContextCompat.getDrawable(this@PlotActivity, R.drawable.circle_marker)
        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)
        mMap?.setOnMapClickListener { p0 ->
            p0?.let { points?.add(it) }
            if (!drawing) {



                marker=mMap?.addMarker(p0?.let { MarkerOptions().position(it) }
                        ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                        ?.anchor(0.5f, 0.5f))
                marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                val rectOptions = PolygonOptions()
                        .strokeWidth(4f)
                        .fillColor(0x7F00FF00)
                        .add(p0)
                polygon = mMap?.addPolygon(rectOptions)
                drawing = true
            } else {
                polygon?.points = points
                marker= mMap?.addMarker(p0?.let { MarkerOptions().position(it) }
                        ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                        ?.anchor(0.5f, 0.5f))
                marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                val convertUtm = CoordinateConversion()
                val northingList=ArrayList<Double>()
                val eastingList=ArrayList<Double>()
                //convertLatLong to UTM
                points?.forEach {

                    val UTM = convertUtm.latLon2UTM(it.latitude, it.longitude)
                    val lastdot = UTM.lastIndexOf("-")
                    val E = UTM.substring(0, lastdot)
                    val N = UTM.substring(lastdot + 1, UTM.length)
                    var  EastingFormat = java.lang.Double.parseDouble(E)
                    var  NorthingFormat = java.lang.Double.parseDouble(N)
                    EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
                    NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0
                    Toast.makeText(this,"$EastingFormat",Toast.LENGTH_SHORT).show()
                    northingList.add(NorthingFormat)
                    eastingList.add(EastingFormat)

                }



                val area= points?.let { calculateArea(it,northingList,eastingList) }
               tvArea.text="$area"

            }
        }

    }

    private fun calculateArea(latLong:ArrayList<LatLng>,northingList:ArrayList<Double>,eastingList:ArrayList<Double>):Double{


        if (latLong.size >= 3) {
            var sum = 0.0
            val area: Double
            val prodx = DoubleArray(latLong.size)
            val prody = DoubleArray(latLong.size)
            val sumxy = DoubleArray(latLong.size)
            for (iteration in latLong.indices) {
                if (iteration < latLong.size - 1) {
                    prodx[iteration] = northingList[iteration] * eastingList[iteration + 1]
                    prody[iteration] = eastingList[iteration] * northingList[iteration + 1]
                }
                if (iteration == latLong.size - 1) {
                    prodx[iteration] = northingList[iteration] * northingList[0]
                    prody[iteration] = eastingList[iteration] * northingList[0]
                }
                sumxy[iteration] = prodx[iteration] - prody[iteration]
            }
            for (l in sumxy) {
                sum += l
            }

            if (sum < 0) {
                sum *= -1.0
            } else {
                sum *= 1.0
            }

            area = sum / 2
val df=DecimalFormat("##.###")


            return df.format(area).toDouble()

        }

        return 0.0

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plot)
        setSupportActionBar(toolbar)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fab.setOnClickListener { view ->
            if (points?.size ?: 0 > 1) {
                val marker:Marker?= hashMapMarker[points?.size]
                marker?.remove()
                hashMapMarker.remove(points?.size)
                points?.size?.minus(1)?.let { points?.removeAt(it) }
                polygon?.points = points


                val convertUtm = CoordinateConversion()
                val northingList=ArrayList<Double>()
                val eastingList=ArrayList<Double>()
                //convertLatLong to UTM
                points?.forEach {

                    val UTM = convertUtm.latLon2UTM(it.latitude, it.longitude)
                    val lastdot = UTM.lastIndexOf("-")
                    val E = UTM.substring(0, lastdot)
                    val N = UTM.substring(lastdot + 1, UTM.length)
                    var  EastingFormat = java.lang.Double.parseDouble(E)
                    var  NorthingFormat = java.lang.Double.parseDouble(N)
                    EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
                    NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0
                    Toast.makeText(this,"$EastingFormat",Toast.LENGTH_SHORT).show()
                    northingList.add(NorthingFormat)
                    eastingList.add(EastingFormat)

                }
                val area= points?.let { calculateArea(it,northingList,eastingList) }
                tvArea.text="$area"
            }

            else{
                val marker:Marker?= hashMapMarker[1]
                marker?.remove()
                hashMapMarker.remove(1)
                points?.clear()
                tvArea.text=""
            }
        }
    }

}
