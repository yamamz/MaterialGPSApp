package com.yamamz.materialgpsapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.yamamz.materialgpsapp.model.LocationModel
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration
import com.yamamz.materialgpsapp.ui.locationAdapter

import java.text.DecimalFormat
import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

/**
* Created by Raymundo T. Melecio on 11/30/2016.
*/


class locDetails : FragmentActivity(), OnMapReadyCallback{
    private var polygon: Polygon?=null
    private var drawing: Boolean=false
    private var points:ArrayList<LatLng>?= ArrayList()
    private var mMap: GoogleMap? = null
    private var realm: Realm? = null
    private var mAdapter: locationAdapter? = null
    private var fileName: String? = null
    private val locationList = RealmList<LocationModel>()
    private val latLngList = ArrayList<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loc_details)

        //initialize map
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val startingIntent = intent
        //get the value on intent and past it to a string
        fileName = startingIntent.getStringExtra("fileName")
        setupRecyclerView()

        /**
         * initialize realm for local database
         */

        realm = Realm.getDefaultInstance()
        //find all save locations on Realm database
        val loc = realm?.where(SaveLocation::class.java)?.findAll()
        //get specific location base on filename that past from intent
        val location = loc?.where()?.equalTo("fileName", fileName)
                ?.findFirst()

        //format text into 2 decimal point
        val df = DecimalFormat("###.###")
        val tv_area = findViewById<TextView>(R.id.area)
        try {
            tv_area.text = String.format("Area:%s m²", df.format(location?.area))
        } catch (ignore: Exception) {

        }
        //lambda that get each location pass it to Location model to populate into recyclerview
        location?.locations?.forEach {
            val locationModel = LocationModel(it.latitude,it.longitude,it.count,it.elevation)
            val latLng = LatLng(it.latitude,it.longitude)
            latLngList.add(latLng)
            locationList.add(locationModel)
            mAdapter?.notifyDataSetChanged()
        }


    }

    //set up  RecyclerView layout and initialize adapter
    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        mAdapter = locationAdapter(locationList, this)
        val mLayoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DeviderItemDecoration(this@locDetails, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = mAdapter
    }

    public override fun onDestroy() {
        super.onDestroy()
        realm?.close()

        if (mMap != null) {
            mMap?.clear()
            Log.e("Yamamz", "On destroy")
        }

        System.gc()
    }

    fun getPolygonCenterPoint(polygonPointsList: ArrayList<LatLng>?): LatLng {
        var centerLatLng: LatLng? = null
        val builder = LatLngBounds.Builder()
        polygonPointsList?.forEach {
            builder.include(it)
        }

        val bounds = builder.build()
        centerLatLng = bounds.center

        return centerLatLng
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //move the camera to the center of the map before going to the target location
        val center =getPolygonCenterPoint(latLngList)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))
        mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID//set hybrid map tile

      async(UI) {
            animateMapFlyGotoLoc(center)
        }


    }


    private fun addMarker(map: GoogleMap, lat: Double, lon: Double,
                          title: String, snippet: String?) {
        map.addMarker(MarkerOptions().position(LatLng(lat, lon))
                .title(title)
                .snippet(snippet))
    }


    private suspend fun animateMapFlyGotoLoc(loc: LatLng) {
        //pause the map so that it will load and sees the flying animation
        delay(1500)
        val position = CameraPosition.Builder()
                .target(loc) // Sets the new camera position
                .zoom(16f) // Sets the zoom
                .bearing(0f) // Rotate the camera
                .tilt(30f)// Set the camera tilt
                .build()// Creates a CameraPosition from the builder
        mMap?.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000, null)

        mMap?.setMaxZoomPreference(19f)//set the maximum zoom level of the map

        //if the lat long size is geater than 1 it will draw a polyline otherwise add marker
        if (latLngList.size > 1) {
            val area = PolygonOptions().addAll(latLngList)
                    .strokeWidth(3f).strokeColor(Color.BLUE)
                    .fillColor(0x7F00FF00)
            mMap?.addPolygon(area)
        } else {
            addMarker(mMap as GoogleMap, latLngList[0].latitude, latLngList[0].longitude, "You", fileName)
        }
    }
}
