package com.yamamz.materialgpsapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
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
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.plotMVP.PlotMVP
import com.yamamz.materialgpsapp.plotMVP.PlotPresenter
import io.realm.Realm
import io.realm.RealmList
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat


class PlotActivity : AppCompatActivity(), OnMapReadyCallback, PlaceSelectionListener, PlotMVP.View {


    var mMap: GoogleMap? = null
    private var polygon: Polygon? = null
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


    override fun putMarkerOnMap(p0: LatLng?) {
        val mDotMarkerBitmap = createBitmapMarker()
        marker = mMap?.addMarker(p0?.let { MarkerOptions().position(it) }
                ?.icon(BitmapDescriptorFactory.fromBitmap(mDotMarkerBitmap))
                ?.anchor(0.5f, 0.5f))

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
            tvArea.text = "Area: 0.0"
            AreaOfPolygon = 0.0
            hashMapMarker.clear()
            //clear all list
            points?.clear()
            Eastings.clear()
            Northings.clear()
            mMap?.clear()

            presenter.drawShape(p0?.latLng)

            //add the first point to the list
            p0?.latLng?.let { points?.add(it) }
            //add the circle marker to the map

            //put the marker the hashmap for removing purpose
            marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }


            val recOptions = createRecOption(p0?.latLng)
            polygon = mMap?.addPolygon(recOptions)

            convertWGSToUTM(p0?.latLng)

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
            convertWGSToUTM(p0?.latLng)

            firstPoint = true
        }



        async(UI) {
            val latLng: LatLng? = p0?.latLng
            presenter.animateMapFlyGotoLoc(latLng)
            animateMapFlyGotoLoc(latLng)
        }
    }

    private fun convertWGSToUTM(p0: LatLng?) {
        val convertUtm = CoordinateConversion()
        val UTM = p0?.latitude?.let { p0?.longitude?.let { it1 -> convertUtm.latLon2UTM(it, it1) } }
        val lastdot = UTM?.lastIndexOf("-")
        val E = lastdot?.let { UTM.substring(0, it) }
        val N = UTM?.length?.let { lastdot?.plus(1)?.let { it1 -> UTM.substring(it1, it) } }
        var EastingFormat = java.lang.Double.parseDouble(E)
        var NorthingFormat = java.lang.Double.parseDouble(N)
        EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
        NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0

        //add the northing and easting to its list
        Northings.add(NorthingFormat)
        Eastings.add(EastingFormat)
    }

    override fun animateMapFlyGotoLoc(loc: LatLng?) {
        //pause the map so that it will load and sees the flying animation
        async(UI) {
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
        val mDotMarkerBitmap = createBitmapMarker()
        //map click listener
        mMap?.setOnMapClickListener { point ->
            //add the Latlong to points List
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
                convertWGSToUTM(point)
                firstPoint = true
                showFab()

            } else {
                //update polygon shape base on latLong
                polygon?.points = points
                //add marker for polygon corner
                presenter.drawShape(point)
                //put marker object to haskmap for removing marker purpose
                marker?.let { points?.size?.minus(0)?.let { it1 -> hashMapMarker.put(it1, it) } }

                //convert wgs84 to UTM projection
                convertWGSToUTM(point)


                val area = calculateArea()

                val df = DecimalFormat("##.####")
                tvArea.text = "Area: ${df.format(area)} m²"
                showFab()

            }
        }

    }


    //this function is for calculating the area of a polygon base on northing/easting coordinates
    private fun calculateArea(): Double {
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


        fab_check.setOnClickListener { view ->
            val finish_points: ArrayList<LatLng>? = ArrayList()

            points?.forEach {
                finish_points?.add(LatLng(it.latitude, it.longitude))

            }
            if (points?.size != 0) {

                val rectOptions = PolygonOptions()
                        .strokeWidth(4f)
                        .fillColor(0x7F00FF00)
                        .add(finish_points?.get(0)?.latitude?.let { LatLng(it, finish_points[0].longitude) })
                polygon = mMap?.addPolygon(rectOptions)
                polygon?.points = finish_points

                hideFab()
                removeMarkers()

                Saveloc(finish_points)



            }



            tvArea.text = "Area: 0.0"
            AreaOfPolygon = 0.0
            hashMapMarker.clear()
            //clear all list
            points?.clear()
            Eastings.clear()
            Northings.clear()

            //mMap?.clear()
            //Toast.makeText(this, finish_points?.size.toString(),Toast.LENGTH_SHORT).show()


        }



        //undo plotting purpose
        fab.setOnClickListener { view ->

            if (points?.size ?: 0 > 0) {
                val marker: Marker? = hashMapMarker[points?.size]
                Toast.makeText(this, hashMapMarker?.size.toString(), Toast.LENGTH_SHORT).show()
                marker?.remove()
                hashMapMarker.remove(points?.size)
                Northings.size.minus(1).let { Northings.removeAt(it) }
                Eastings.size.minus(1).let { Eastings.removeAt(it) }
                points?.size?.minus(1)?.let { points?.removeAt(it) }

                //Toast.makeText(this, points?.size.toString(),Toast.LENGTH_SHORT).show()
                //update the area upon deleting corner of a polygon
                calculateArea()

                val df = DecimalFormat("##.####")
                val area = calculateArea()
                tvArea.text = "Area: ${df.format(area)}m²"

                if (points?.size == 0) {
                    firstPoint = false
                    tvArea.text = "Area: 0.0"
                    AreaOfPolygon = 0.0
                } else {
                    polygon?.points = points

                }
            } else {
                marker?.remove()


                Toast.makeText(this, "No recent Plot, Please plot on the map by tapping the map", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun generateCSVOnSD(context: Context, sFileName:String , sBody:ArrayList<LatLng>? ) {

        try {
            val root = File(Environment.getExternalStorageDirectory(), "CSV")
            if (!root.exists()) {
                root.mkdirs()
            }

            val csvfile = File(root,sFileName)
            val writer = FileWriter(csvfile)
            writer.append("Latitude,Longitude \n")
            sBody?.forEach{
                writer.append(it.latitude.toString().plus(",".plus(it.longitude).plus("\n")))
            }

            writer.flush()
            writer.close()
            Snackbar.make(coordinator, "Saved to".plus(root.absolutePath), Snackbar.LENGTH_LONG)
                    .setAction("OK", {

                    })
                    .setActionTextColor(Color.RED)
                    .show()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    fun hideFab() {
        if (isFabShowing && fab_check != null) {
            isFabShowing = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                val point = Point()
                this.window.windowManager.defaultDisplay.getSize(point)
                val translation = fab_check?.y?.minus(point.y)
                if (translation != null) {
                    fab_check?.animate()?.translationYBy(-translation)?.start()
                }
            } else {
                val animation = AnimationUtils.makeOutAnimation(this, true)
                animation.fillAfter = true
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        fab_check?.isClickable = false
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                fab?.startAnimation(animation)
            }
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    fun showFab() {
        if (!isFabShowing && fab_check != null) {
            isFabShowing = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                fab_check?.animate()?.translationY(0f)?.start()
            } else {
                val animation = AnimationUtils.makeInAnimation(this, false)
                animation.fillAfter = true
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        fab_check?.isClickable = true
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                fab_check?.startAnimation(animation)
            }
        }
    }



    private fun removeMarkers() {
        hashMapMarker.forEach {
            it.value.remove()
        }
        hashMapMarker.clear()
    }

    fun Saveloc(finish_points:ArrayList<LatLng>?) {

   var filename: CharSequence? = null
        if (finish_points?.size?:0 >= 1)
            MaterialDialog.Builder(this).title(R.string.input).inputType(InputType.TYPE_CLASS_TEXT).input(R.string.input_hint, R.string.input_prefill

            ) { dialog, input ->
                filename = input

                if (filename?.length?:0 > 0) {
       val locationList = RealmList<LocationModel>()
                   finish_points?.forEach {
                        var x=0.0
                        val location = LocationModel(it.latitude,
                               it.longitude,x.toString(),0.0)
                        locationList.add(location)
                        x += 1

                    }
                    save(filename,locationList,finish_points)
                }

            }.show()
    }

    internal fun save(filename:CharSequence?,locationList:RealmList<LocationModel>,finish_points:ArrayList<LatLng>?) {
        val area = AreaOfPolygon
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
                Toast.makeText(this, "Save Successfully", Toast.LENGTH_SHORT).show()
                val dateFormat= SimpleDateFormat("yyyy-MM-dd")
                val now= Date()
                val file_name=dateFormat.format(now)
                generateCSVOnSD(this,filename.toString().plus(file_name.plus("WGS_84.csv")),finish_points)
                //addlocation()
                //locationList.clear()


            })

        } catch (ignored: Exception) {
        } finally {
            realm.close()
        }
    }

}
