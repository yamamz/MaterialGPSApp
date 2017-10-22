package com.yamamz.materialgpsapp

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
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
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay


class PlotActivity : AppCompatActivity(), OnMapReadyCallback, PlaceSelectionListener {
    @SuppressLint("SetTextI18n")
    override fun onPlaceSelected(p0: Place?) {

        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
        val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        val shape = ContextCompat.getDrawable(this@PlotActivity, R.drawable.circle_marker)
        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)

        if (mMap == null) {
            return
        }
        if(points != null){

            tvArea.text="Area: 0.0"
            AreaOfPolygon=0.0
            hashMapMarker.clear()
            //clear all list
            points?.clear()
            Eastings.clear()
            Northings.clear()
            mMap?.clear()


            p0?.latLng?.let { points?.add(it) }

            //add the circle marker to the map
            marker = mMap?.addMarker(p0?.latLng?.let { MarkerOptions().position(it) }
                    ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                    ?.anchor(0.5f, 0.5f))
            //put the marker the hashmap for removing purpose
            marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

            //initialize polygon Options
            val rectOptions = PolygonOptions()
                    .strokeWidth(4f)
                    .fillColor(0x7F00FF00)
                    .add(p0?.latLng)
            polygon = mMap?.addPolygon(rectOptions)

            //convert wgs84 to UTM projection
            val convertUtm = CoordinateConversion()
            val UTM = p0?.latLng?.latitude?.let { p0.latLng?.longitude?.let { it1 -> convertUtm.latLon2UTM(it, it1) } }
            val lastdot = UTM?.lastIndexOf("-")
            val E = lastdot?.let { UTM?.substring(0, it) }
            val N = UTM?.length?.let { lastdot?.plus(1)?.let { it1 -> UTM.substring(it1, it) } }
            var EastingFormat = java.lang.Double.parseDouble(E)
            var NorthingFormat = java.lang.Double.parseDouble(N)
            EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
            NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0

            //add the northing and easting to its list
            Northings.add(NorthingFormat)
            Eastings.add(EastingFormat)
            firstPoint = true




        }



        else {

            p0?.latLng?.let { points?.add(it) }

            //add the circle marker to the map
            marker = mMap?.addMarker(p0?.latLng?.let { MarkerOptions().position(it) }
                    ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                    ?.anchor(0.5f, 0.5f))
            //put the marker the hashmap for removing purpose
            marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

            //initialize polygon Options
            val rectOptions = PolygonOptions()
                    .strokeWidth(4f)
                    .fillColor(0x7F00FF00)
                    .add(p0?.latLng)
            polygon = mMap?.addPolygon(rectOptions)

            //convert wgs84 to UTM projection
            val convertUtm = CoordinateConversion()
            val UTM = p0?.latLng?.latitude?.let { p0?.latLng?.longitude?.let { it1 -> convertUtm.latLon2UTM(it, it1) } }
            val lastdot = UTM?.lastIndexOf("-")
            val E = lastdot?.let { UTM?.substring(0, it) }
            val N = UTM?.length?.let { lastdot?.plus(1)?.let { it1 -> UTM.substring(it1, it) } }
            var EastingFormat = java.lang.Double.parseDouble(E)
            var NorthingFormat = java.lang.Double.parseDouble(N)
            EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
            NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0

            //add the northing and easting to its list
            Northings.add(NorthingFormat)
            Eastings.add(EastingFormat)
            firstPoint = true
        }

            val  latLng: LatLng? = p0?.latLng

        async(UI) {
            animateMapFlyGotoLoc(latLng)
        }
    }

    private suspend fun animateMapFlyGotoLoc(loc: LatLng?) {
        //pause the map so that it will load and sees the flying animation
        delay(1500)
        val position = CameraPosition.Builder()
                .target(loc) // Sets the new camera position
                .zoom(19f) // Sets the zoom
                .bearing(0f) // Rotate the camera
                .tilt(30f)// Set the camera tilt
                .build()// Creates a CameraPosition from the builder
        mMap?.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000, null)

        mMap?.setMaxZoomPreference(19f)//set the maximum zoom level of the map


}


override fun onError(p0: Status?) {

    }

    var mMap:GoogleMap?=null
    private var polygon: Polygon?=null
    val hashMapMarker = HashMap<Int, Marker>()
    private var firstPoint: Boolean=false
    private var points: ArrayList<LatLng>?= ArrayList()
    private var  marker: Marker?=null
    internal var Northings = ArrayList<Double>()
    internal var Eastings = ArrayList<Double>()
    private var AreaOfPolygon: Double=0.toDouble()


    @SuppressLint("SetTextI18n")
    override fun onMapReady(p0: GoogleMap?) {

        mMap = p0
        //move the camera to the center of the map before going to the target location
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))
        mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID//set hybrid map tile


        //create a marker bitmap
        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
        val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        val shape = ContextCompat.getDrawable(this@PlotActivity, R.drawable.circle_marker)
        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)

        //map click listener
        mMap?.setOnMapClickListener { p0 ->
            //add the Latlong to points List
            p0?.let { points?.add(it) }

            if (!firstPoint) {
                //add the circle marker to the map
                marker=mMap?.addMarker(p0?.let { MarkerOptions().position(it) }
                        ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                        ?.anchor(0.5f, 0.5f))
                //put the marker the hashmap for removing purpose
                marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                //initialize polygon Options
                val rectOptions = PolygonOptions()
                        .strokeWidth(4f)
                        .fillColor(0x7F00FF00)
                        .add(p0)
                polygon = mMap?.addPolygon(rectOptions)

                //convert wgs84 to UTM projection
                val convertUtm = CoordinateConversion()
                val UTM = convertUtm.latLon2UTM(p0.latitude,p0.longitude)
                val lastdot = UTM.lastIndexOf("-")
                val E = UTM.substring(0, lastdot)
                val N = UTM.substring(lastdot + 1, UTM.length)
                var  EastingFormat = java.lang.Double.parseDouble(E)
                var  NorthingFormat = java.lang.Double.parseDouble(N)
                EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
                NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0

                //add the northing and easting to its list
                Northings.add(NorthingFormat)
                Eastings.add(EastingFormat)
                firstPoint = true
            } else {
                //update polygon shape base on latLong
                polygon?.points = points
                //add marker for polygon corner
                marker= mMap?.addMarker(p0?.let { MarkerOptions().position(it) }
                        ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                        ?.anchor(0.5f, 0.5f))
                //put marker object to haskmap for removing marker purpose
                marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                //convert wgs84 to UTM projection
                val convertUtm = CoordinateConversion()
                val UTM = convertUtm.latLon2UTM(p0.latitude,p0.longitude)
                val lastdot = UTM.lastIndexOf("-")
                val E = UTM.substring(0, lastdot)
                val N = UTM.substring(lastdot + 1, UTM.length)
                var  EastingFormat = java.lang.Double.parseDouble(E)
                var  NorthingFormat = java.lang.Double.parseDouble(N)
                EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
                NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0
                //add northing easting to the list for calculation of area
                Northings.add(NorthingFormat)
                Eastings.add(EastingFormat)


                calculateArea()
                val df=DecimalFormat("##.####")
                tvArea.text="Area: ${df.format(AreaOfPolygon)} m²"

            }
        }

    }

    //this function is for calculating the area of a polygon base on northing/easting coordinates
    internal fun calculateArea() {
        if (Northings.size >= 2) {
            var sum = 0.0
            val area: Double
            val prodx = DoubleArray(Northings.size)
            val prody = DoubleArray(Northings.size)
            val sumxy = DoubleArray(Northings.size)
            for (iteration in Northings.indices) {
                if (iteration < Northings.size - 1) {
                    prodx[iteration] = Northings[iteration] * Eastings[iteration + 1]
                    prody[iteration] = Eastings[iteration] * Northings[iteration + 1]
                }
                if (iteration == Northings.size - 1) {
                    prodx[iteration] = Northings[iteration] * Eastings[0]
                    prody[iteration] = Eastings[iteration] * Northings[0]
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

            AreaOfPolygon = area

        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plot)
        val autocompleteFragment = fragmentManager.findFragmentById(R.id.autocomplete_fragment) as PlaceAutocompleteFragment

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //undo plotting purpose
        fab.setOnClickListener { view ->
            if (points?.size ?: 0 > 0) {
                val marker:Marker?= hashMapMarker[points?.size]
                marker?.remove()
                hashMapMarker.remove(points?.size)
                Northings.size.minus(1).let { Northings.removeAt(it) }
                Eastings.size.minus(1).let { Eastings.removeAt(it) }
                points?.size?.minus(1)?.let { points?.removeAt(it) }



                //update the area upon deleting corner of a polygon
                calculateArea()
                val df=DecimalFormat("##.####")
                tvArea.text="Area: ${df.format(AreaOfPolygon)}m²"

                if(points?.size==0){
                    firstPoint=false
                    tvArea.text="Area: 0.0"
                    AreaOfPolygon=0.0
                }
                else{
                    polygon?.points = points
                }
            }
            else{

                Toast.makeText(this, "No recent Plot, Please plot on the map by tapping the map",Toast.LENGTH_SHORT).show()
            }
        }
    }

}
