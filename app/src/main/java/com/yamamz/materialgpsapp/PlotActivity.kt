package com.yamamz.materialgpsapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.clans.fab.FloatingActionMenu
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
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolygonOptions
import com.yamamz.materialgpsapp.model.LocationModel
import com.yamamz.materialgpsapp.model.MapObject
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.plotMVP.PlotMVP
import com.yamamz.materialgpsapp.plotMVP.PlotPresenter
import io.realm.Realm
import io.realm.RealmList

import java.text.SimpleDateFormat


class PlotActivity : AppCompatActivity(), OnMapReadyCallback, PlaceSelectionListener, PlotMVP.View {


    var mMap: GoogleMap? = null
    private var polygon: Polygon? = null
    private var polyline: Polyline?=null
    @SuppressLint("UseSparseArrays")
    private val hashMapMarker = HashMap<Int, Marker>()
    private var firstPoint: Boolean = false
    private var points: ArrayList<LatLng>? = ArrayList()
    private var marker: Marker? = null
    private var Northings = ArrayList<Double>()
    private var Eastings = ArrayList<Double>()
    private var AreaOfPolygon: Double = 0.toDouble()
    private var isFabShowing = true
    private val presenter = PlotPresenter(this)

    private var menus:ArrayList<FloatingActionMenu>?  = java.util.ArrayList()
    private val mUiHandler = Handler()
    private var latLngs= ArrayList<LatLng>()
    private var mapObjects= ArrayList<MapObject>()


    private var isAPolylineActive: Boolean= true

    override fun clearAllvalues() {
        tvArea.text = "Area: 0.0"
        AreaOfPolygon = 0.0
        hashMapMarker.clear()
        //clear all list
        points?.clear()
        Eastings.clear()
        Northings.clear()
        firstPoint = false
    }

    override fun putMarkerOnMap(p0: LatLng?) {
        val mDotMarkerBitmap = createBitmapMarker()
        marker = mMap?.addMarker(p0?.let { MarkerOptions().position(it) }
                ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                ?.anchor(0.5f, 0.5f))

    }

    override fun addNorthingEasting(northingFormat: Double, eastingFormat: Double) {
        Northings.add(northingFormat)
        Eastings.add(eastingFormat)
           }


    override fun showSnackbar(msg: String) {
        Snackbar.make(coordinator,msg , Snackbar.LENGTH_LONG)
                .setAction("OK", {
                })
                .setActionTextColor(Color.RED)
                .show()
    }

    fun createBitmapMarker(): Bitmap {
        val px = resources.getDimensionPixelSize(R.dimen.map_dot_marker_size)
        val mDotMarkerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mDotMarkerBitmap)
        val shape = ContextCompat.getDrawable(this@PlotActivity, R.drawable.circle_marker)
        shape.setBounds(0, 0, mDotMarkerBitmap.width, mDotMarkerBitmap.height)
        shape.draw(canvas)
        return mDotMarkerBitmap
    }


    override fun createRecOption(p0: LatLng?): PolygonOptions {
        val rectOptions = PolygonOptions()
                .strokeWidth(4f)
                .fillColor(0x7F00FF00)
                .add(p0)
        return rectOptions
    }


    @SuppressLint("SetTextI18n")
    override fun onPlaceSelected(p0: Place?) {

        if (mMap == null) {
            return
        }
        if (points != null) {
            //presenter.setTextArea()
            presenter.clearAllvalues()
            mMap?.clear()
            presenter.drawShape(p0?.latLng)

            //add the first point to the list
            p0?.latLng?.let { points?.add(it) }
            //add the circle marker to the map

            //put the marker the hashmap for removing purpose
            marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

            val recOptions = createRecOption(p0?.latLng)
            polygon = mMap?.addPolygon(recOptions)

            presenter.convertWGSToUTM(p0?.latLng)

            //setting true so the logic determines that the second marker is not the first
            firstPoint = true
        } else {

            p0?.latLng?.let { points?.add(it) }
            //add the circle marker to the map
            presenter.drawShape(p0?.latLng)
            //put the marker the hashmap for removing purpose
            marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }


            polygon = mMap?.addPolygon(createRecOption(p0?.latLng))

            //convert wgs84 to UTM projection
            presenter.convertWGSToUTM(p0?.latLng)

            firstPoint = true
        }



        async(UI) {
            val latLng: LatLng? = p0?.latLng
            presenter.animateMapFlyGotoLoc(latLng)
            animateMapFlyGotoLoc(latLng)
        }
    }



    override fun animateMapFlyGotoLoc(latLng: LatLng?) {
        //pause the map so that it will load and sees the flying animation
        async(UI) {
            delay(1500)
            val position = CameraPosition.Builder()
                    .target(latLng) // Sets the new camera position
                    .zoom(19f) // Sets the zoom
                    .bearing(0f) // Rotate the camera
                    .tilt(30f)// Set the camera tilt
                    .build()// Creates a CameraPosition from the builder
            mMap?.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 2000, null)

            mMap?.setMaxZoomPreference(30f)//set the maximum zoom level of the map

        }
    }


    override fun onError(p0: Status?) {

    }


    @SuppressLint("SetTextI18n")
    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0

        //move the camera to the center of the map before going to the target location
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))
        mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID//set hybrid map tile
        //create a marker bitmap
        //val mDotMarkerBitmap = createBitmapMarker()
        //map click listener
        mMap?.setOnMapClickListener { point ->
            //add the Latlong to points List

            if (isAPolylineActive) {
                point?.let { points?.add(it) }
                if (!firstPoint) {
                    //add the circle marker to the map
                    presenter.drawShape(point)

                    //put the marker the hashmap for removing purpose
                    marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                    //initialize polygon Options
                    val rectOptions = PolygonOptions()
                            .strokeWidth(4f)
                            .fillColor(0x7F00FF00)
                            .add(point)

                    polygon = mMap?.addPolygon(rectOptions)

                    //convert wgs84 to UTM projection
                    presenter.convertWGSToUTM(point)
                    firstPoint = true
                    //showFab()

                } else {
                    //update polygon shape base on latLong
                    polygon?.points = points
                    //add marker for polygon corner
                    presenter.drawShape(point)
                    //put marker object to haskmap for removing marker purpose
                    marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                    //convert wgs84 to UTM projection
                    presenter.convertWGSToUTM(point)


                    val area = calculateArea(Northings,Eastings)

                    val df = DecimalFormat("##.####")
                    tvArea.text = "Area: ${df.format(area)} m²"
                    //showFab()

                }
            }


            else {
                point?.let { points?.add(it) }
                if (!firstPoint) {
                    polyline = mMap?.addPolyline(PolylineOptions()
                            .clickable(true)
                            .color(Color.MAGENTA)
                            .width(5f)
                            .jointType(JointType.ROUND)
                            .add(point))
                    presenter.drawShape(point)
                    //put the marker the hashmap for removing purpose
                    marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }
                    firstPoint = true
                }
                else{
                    presenter.drawShape(point)
                    polyline?.points=points
                    marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                }
            }
        }

    }


    //this function is for calculating the area of a polygon base on northing/easting coordinates
    private fun calculateArea(Northings:ArrayList<Double>,Eastings:ArrayList<Double>): Double {
        if (Northings.size >= 2) {
            val area: Double
            val prodx = DoubleArray(Northings.size)
            val prody = DoubleArray(Northings.size)
            val sumxy = DoubleArray(Northings.size)
            for (i in Northings.indices) {
                if (i < Northings.size - 1) {
                    prodx[i] = Northings[i] * Eastings[i + 1]
                    prody[i] = Eastings[i] * Northings[i + 1]
                }
                if (i == Northings.size - 1) {
                    prodx[i] = Northings[i] * Eastings[0]
                    prody[i] = Eastings[i] * Northings[0]
                }
                sumxy[i] = prodx[i] - prody[i]
            }
            var sum = sumxy.sum()

            sum *= if (sum < 0) {
                -1.0
            } else {
                1.0
            }

            area = sum / 2

            AreaOfPolygon = area

            return AreaOfPolygon
        } else return 0.0
    }


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
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

        //val context = ContextThemeWrapper(this, R.style.MenuButtonsStyle)

        menu_green?.hideMenuButton(false)
        menus?.add(menu_green)

        var delay = 400
       menus?.forEach {
            mUiHandler.postDelayed(Runnable { it.showMenuButton(true) }, delay.toLong())
            delay += 150
        }

        //createCustomAnimation()

        fab_poly_line_or_gon.setOnClickListener {
      when(isAPolylineActive){

          true ->{

              fab_poly_line_or_gon.setImageResource(R.drawable.ic_timeline_white_24dp)
              fab_poly_line_or_gon.labelText="Polyline is active"
              finish_points()
              isAPolylineActive=false


          }

          false -> {

              fab_poly_line_or_gon.setImageResource(R.drawable.ic_format_shapes_white_24dp)
              fab_poly_line_or_gon.labelText="Polygon is active"
              finish_points()
              isAPolylineActive=true

          }
      }
        }

        fab_save.setOnClickListener {

            var filename: CharSequence? = null
            if (mapObjects.size>= 1)
                if(points?.size==0) {
                    MaterialDialog.Builder(this).title(R.string.input).inputType(InputType.TYPE_CLASS_TEXT).input(R.string.input_hint, R.string.input_prefill

                    ) { dialog, input ->
                        filename = input

                        if (filename?.length ?: 0 > 0) {

                            mapObjects.forEachIndexed { i, e ->

                                Saveloc(e.latLngList, filename.toString(), i, e.area)

                            }


                        }

                    }.show()
                }
            else{

                presenter.showSnackBar("finish first your active plot")
            }

            else
                presenter.showSnackBar("you dont have finish map object finish first to save")

                        //loop each object to get each arrayList of LatLong

        }

        fab_check.setOnClickListener { view ->
         finish_points()
        }

        fab_gpsTracks.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

fab_export.setOnClickListener {
    var filename: CharSequence? = null
    if ( mapObjects.size >= 1)
    //checking if there no active plot
        if(points?.size==0) {
            MaterialDialog.Builder(this).title(R.string.input).inputType(InputType.TYPE_CLASS_TEXT).input(R.string.input_hint, R.string.input_prefill

            ) { dialog, input ->
                filename = input

                if (filename?.length ?: 0 > 0) {
                    //loop each object to get each arrayList of LatLong
                    mapObjects.forEach {
                        //random the current date int for unique value

                        presenter.generateCSV(filename.toString(), it.latLngList)

                        //generateCSVOnSD(filename.toString().plus(intDate).plus("WGS_84.csv"),it.latLngList)

                    }

                }

            }.show()

        }
    //if there is active plot
    else{

            presenter.showSnackBar("finish first your active plot")
        }

    else
        presenter.showSnackBar("you dont have finish map object finish first to export")
}

fab_clearMap.setOnClickListener {

    presenter.clearAllvalues()
    mMap?.clear()
}
        //undo plotting purpose
        fab.setOnClickListener { view ->

            if (points?.size ?: 0 > 0) {
                val marker: Marker? = hashMapMarker[points?.size]
                marker?.remove()
                hashMapMarker.remove(points?.size)
                //
                if(isAPolylineActive) {
                    Northings.size.minus(1).let { Northings.removeAt(it) }
                    Eastings.size.minus(1).let { Eastings.removeAt(it) }
                }
                points?.size?.minus(1)?.let { points?.removeAt(it) }


                //update the area upon deleting corner of a polygon
                calculateArea(Northings,Eastings)

                val df = DecimalFormat("##.####")
                val area = calculateArea(Northings,Eastings)
                tvArea.text = "Area: ${df.format(area)}m²"

                if (points?.size == 0) {
                    firstPoint = false
                    tvArea.text = "Area: 0.0"
                    AreaOfPolygon = 0.0
                } else {
                    if(isAPolylineActive)
                    polygon?.points = points
                    else
                        polyline?.points=points


                }
            } else {
                marker?.remove()

                 }
        }
    }

    fun finish_points(){
        val  fin_points=ArrayList<LatLng>()

        points?.forEach {
            fin_points.add(LatLng(it.latitude, it.longitude))
        }

        if (points?.size != 0 && isAPolylineActive) {
            try {
                val rectOptions = PolygonOptions()
                        .strokeWidth(4f)
                        .fillColor(0x7F00FF00)
                        .add(fin_points[0].latitude.let { LatLng(it, fin_points[0].longitude) })
                polygon = mMap?.addPolygon(rectOptions)
                polygon?.points = fin_points

                // hideFab()
                removeMarkers()
                val area = calculateArea(Northings, Eastings)
                val mapObject = MapObject(fin_points, area,"Polygon")
                mapObjects.add(mapObject)

                presenter.clearAllvalues()

            }
            catch (e: IndexOutOfBoundsException){

            }

        }

        else{
            try {
                polyline = mMap?.addPolyline(PolylineOptions()
                        .clickable(true)
                        .color(Color.MAGENTA)
                        .width(5f)
                        .jointType(JointType.ROUND)
                        .add(fin_points[0].latitude.let { LatLng(it, fin_points[0].longitude) }))
                polyline?.points = fin_points

                removeMarkers()

                val mapObject = MapObject(fin_points, 0.0,"Polyline")
                mapObjects.add(mapObject)

                presenter.clearAllvalues()
            }
            catch (e: IndexOutOfBoundsException){

            }

        }


    }

//    fun createCustomAnimation() {
//        val set = AnimatorSet()
//
//        val scaleOutX = ObjectAnimator.ofFloat(menu_green.menuIconView, "scaleX", 1.0f, 0.2f)
//        val scaleOutY = ObjectAnimator.ofFloat(menu_green.menuIconView, "scaleY", 1.0f, 0.2f)
//        val scaleInX = ObjectAnimator.ofFloat(menu_green.menuIconView, "scaleX", 0.2f, 1.0f)
//        val scaleInY = ObjectAnimator.ofFloat(menu_green.menuIconView, "scaleY", 0.2f, 1.0f)
//
//        scaleOutX.duration = 50
//        scaleOutY.duration = 50
//        scaleInX.duration = 150
//        scaleInY.duration = 150
//
//        scaleInX.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationStart(animation: Animator) {
//                menu_green?.menuIconView?.setImageResource(if (menu_green.isOpened)
//                    R.drawable.ic_close_white_24dp
//                else
//                    R.drawable.fab_add)
//            }
//        })
//
//        set.play(scaleOutX).with(scaleOutY)
//        set.play(scaleInX).with(scaleInY).after(scaleOutX)
//        set.interpolator = OvershootInterpolator(2f)
//        menu_green.iconToggleAnimatorSet = set
//    }



//    @SuppressLint("ObsoleteSdkInt")
//    fun hideFab() {
//        if (isFabShowing && fab_check != null) {
//            isFabShowing = false
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                val point = Point()
//                this.window.windowManager.defaultDisplay.getSize(point)
//                val translation = fab_check?.y?.minus(point.y)
//                if (translation != null) {
//                    fab_check?.animate()?.translationYBy(-translation)?.start()
//                }
//            } else {
//                val animation = AnimationUtils.makeOutAnimation(this, true)
//                animation.fillAfter = true
//                animation.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationStart(animation: Animation) {
//
//                    }
//
//                    override fun onAnimationEnd(animation: Animation) {
//                        fab_check?.isClickable = false
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation) {
//
//                    }
//                })
//                fab?.startAnimation(animation)
//            }
//        }
//    }
//
//
//    @SuppressLint("ObsoleteSdkInt")
//    fun showFab() {
//        if (!isFabShowing && fab_check != null) {
//            isFabShowing = true
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                fab_check?.animate()?.translationY(0f)?.start()
//            } else {
//                val animation = AnimationUtils.makeInAnimation(this, false)
//                animation.fillAfter = true
//                animation.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationStart(animation: Animation) {}
//
//                    override fun onAnimationEnd(animation: Animation) {
//                        fab_check?.isClickable = true
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation) {}
//                })
//                fab_check?.startAnimation(animation)
//            }
//        }
//    }



    private fun removeMarkers() {
        hashMapMarker.forEach {
            it.value.remove()
        }
        hashMapMarker.clear()
    }

    fun Saveloc(finish_points:ArrayList<LatLng>?,filename:String,counter:Int,area:Double) {

        val locationList = RealmList<LocationModel>()

        //lop the finish point to store in rearlmList for saving pupose
        finish_points?.forEachIndexed {i,it->

            val location = LocationModel(it.latitude,
                    it.longitude,(i+1).toString(),0.0)
            locationList.add(location)


        }//call save function

        save(filename.plus("-").plus(counter),locationList,area)
    }

    private fun save(filename:CharSequence?, locationList:RealmList<LocationModel>,area: Double) {

        val saveLocation = SaveLocation(filename?.toString(), locationList, area)
        val realm = Realm.getDefaultInstance()
        try {
            realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                try {
                    bgRealm.copyToRealm(saveLocation)
                } catch (e: Exception) {
                    this.runOnUiThread {
                        Toast.makeText(this, "your filename is already exist", Toast
                                .LENGTH_SHORT)
                                .show()
                    }
                }
            }, Realm.Transaction.OnSuccess {
                presenter.showSnackBar("Save Succesfully")

            })

        } catch (ignored: Exception) {
        } finally {
            realm.close()
        }
    }

}
