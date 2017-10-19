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


class locDetails : FragmentActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private var realm: Realm? = null
    private var mAdapter: locationAdapter? = null
    private var fileName: String? = null
    private val locationList = RealmList<LocationModel>()
    private val latLngList = ArrayList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loc_details)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val startingIntent = intent
        fileName = startingIntent.getStringExtra("fileName")
        setupRecyclerView()

        /**
         * initialize realm for local database
         */

        realm = Realm.getDefaultInstance()

        val loc = realm?.where(SaveLocation::class.java)?.findAll()
        val location = loc?.where()?.equalTo("fileName", fileName)
                ?.findFirst()
        val df = DecimalFormat("###.###")
        val tv_area = findViewById<TextView>(R.id.area)
        try {
            tv_area.text = String.format("Area:%s m²", df.format(location?.area))
        } catch (ignore: Exception) {

        }



        location?.locations?.forEach {

            val locationModel = LocationModel(it.latitude,it.longitude,it.count,it.elevation)
            val latLng = LatLng(it.latitude,it.longitude)
            latLngList.add(latLng)
            locationList.add(locationModel)
            mAdapter?.notifyDataSetChanged()
        }


    }


    internal fun setupRecyclerView() {
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val center = LatLng(latLngList[0].latitude, latLngList[0].longitude)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.00, 0.00), 0f))
        mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID

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

        delay(1500)

        val position = CameraPosition.Builder()
                .target(loc) // Sets the new camera position
                .zoom(19f) // Sets the zoom
                .bearing(0f) // Rotate the camera
                .tilt(30f)// Set the camera tilt
                .build()// Creates a CameraPosition from the builder
        mMap?.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 2000, null)

        mMap?.setMaxZoomPreference(19f)

        if (latLngList.size > 1) {
            val area = PolygonOptions().addAll(latLngList)
                    .strokeWidth(3f).strokeColor(Color.BLUE)
                    .fillColor(Color.BLUE)
            mMap?.addPolygon(area)
        } else {
            addMarker(mMap as GoogleMap, latLngList[0].latitude, latLngList[0].longitude, "You", fileName)
        }


    }

}
