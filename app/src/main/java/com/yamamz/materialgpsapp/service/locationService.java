package com.yamamz.materialgpsapp.service;


import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Raymundo T. Melecio on 11/30/2016.
 */

public class locationService extends Service {
    private static final String TAG = "yamamz";


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;
    private Location mLocation;


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

//        Log.e(TAG, "onCreate");
//        initializeLocationManager();
//
//        try {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                    mLocationListeners[0]);
//        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
//        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
//        }
//
//        try {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                    mLocationListeners[1]);
//        } catch (java.lang.SecurityException ex) {
//            Log.i(TAG, "fail to request location update, ignore", ex);
//        } catch (IllegalArgumentException ex) {
//            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
//        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        requestLocationUpdates();
    }

    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopSelf();
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }


    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }


    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent("getlocation");
        intent.putExtra("location", location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        mServiceHandler.removeCallbacksAndMessages(null);
        removeLocationUpdates();
    }


}