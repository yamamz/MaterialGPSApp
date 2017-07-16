package com.yamamz.materialgpsapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class locDetails extends AbstractMapActivity implements
        OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private boolean needsInit = false;

    private Realm realm;
    private RecyclerView recyclerView;
    private locationAdapter mAdapter;
    private String fileName;
    private SupportMapFragment mapFragment;
    private GoogleMap mGoogleMap;
    private RealmList<LocationModel> locationList = new RealmList<>();
    private List<LatLng> latLngList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (readyToGo()) {
            System.gc();
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


        }


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


    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(latLngList.get(0).latitude,
                       latLngList.get(0).longitude));
        CameraUpdate zoom= CameraUpdateFactory.zoomTo(18);
        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
if(latLngList.size()>1) {
    PolygonOptions area = new PolygonOptions().addAll(latLngList)
            .strokeWidth(3).strokeColor(Color.BLUE);
    googleMap.addPolygon(area);
}
else{
    addMarker(googleMap,latLngList.get(0).latitude,latLngList.get(0).longitude,"You",fileName);
}
        googleMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        googleMap.setOnInfoWindowClickListener(this);

        System.gc();



    }


    private void addMarker(GoogleMap map, double lat, double lon,
                           String title, String snippet) {
        map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title(title)
                .snippet(snippet));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        switch (item.getItemId()){

            case R.id.action_settings:
                break;
            case R.id.icnRemove:
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        RealmResults<SaveLocation> results =bgRealm.where(SaveLocation.class)
                                .equalTo("fileName",
                                        fileName).findAll();
                        results.deleteAllFromRealm();
                    }
                }, new Realm.Transaction.OnSuccess() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(locDetails.this, "Delete successfully on location " +
                                fileName, Toast
                                .LENGTH_LONG).show();
                        Intent intent = new Intent(locDetails.this, MainActivity.class);
                        startActivity(intent);

                    }
                });
                break;

        }

        return super.onOptionsItemSelected(item);
    }



}
