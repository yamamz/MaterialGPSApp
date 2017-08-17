package com.yamamz.materialgpsapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.yamamz.materialgpsapp.model.LocationModel;
import com.yamamz.materialgpsapp.model.SaveLocation;
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration;
import com.yamamz.materialgpsapp.ui.locationAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;



public class locDetails extends FragmentActivity implements OnMapReadyCallback {
    private boolean needsInit = false;
    private GoogleMap mMap;
    private Realm realm;
    private RecyclerView recyclerView;
    private locationAdapter mAdapter;
    private String fileName;
    private SupportMapFragment mapFragment;
    private RealmList<LocationModel> locationList = new RealmList<>();
    private List<LatLng> latLngList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_details);




            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);



        Intent startingIntent = getIntent();
        fileName = startingIntent.getStringExtra("fileName");


        setupRecyclerView();

        /**
         * initialize realm for local database
         */
        Realm.init(this);
        realm = Realm.getDefaultInstance();

        final RealmResults<SaveLocation> loc = realm.where(SaveLocation.class).findAll();
        SaveLocation location = loc.where().equalTo("fileName", fileName)
                .findFirst();
        DecimalFormat df = new DecimalFormat("###.###");
        TextView tv_area = (TextView) findViewById(R.id.area);
        try {
            tv_area.setText(String.format("Area:%s m²", df.format(location.getArea())));
        } catch (Exception ignore) {

        }

        for (int i = 0; i < location.getLocations().size(); i++) {

            LocationModel locationModel = new LocationModel(location.getLocations().get(i).getLatitude(), location.getLocations().get(i).getLongitude(), location.getLocations().get(i).getCount(), location.getLocations().get(i).getElevation());

            LatLng latLng = new LatLng(location.getLocations().get(i).getLatitude(), location.getLocations().get(i).getLongitude());

            latLngList.add(latLng);

            locationList.add(locationModel);
            mAdapter.notifyDataSetChanged();
        }

        if (savedInstanceState == null) {
            needsInit = true;
        }
    }


    void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new locationAdapter(locationList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DeviderItemDecoration(locDetails.this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();

        if (mMap != null) {
            mMap.clear();
            Log.e("Yamamz", "On destroy");
        }

System.gc();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(latLngList.get(0).latitude,latLngList.get(0).longitude));
        CameraUpdate zoom= CameraUpdateFactory.zoomTo(17);
        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    mMap.setMaxZoomPreference(19);
if(latLngList.size()>1) {
    PolygonOptions area = new PolygonOptions().addAll(latLngList)
            .strokeWidth(3).strokeColor(Color.BLUE);
    mMap.addPolygon(area);
}
else{
    addMarker(mMap,latLngList.get(0).latitude,latLngList.get(0).longitude,"You",fileName);
}


    }


    private void addMarker(GoogleMap map, double lat, double lon,
                           String title, String snippet) {
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title(title)
                .snippet(snippet));
    }



}
