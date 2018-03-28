package com.yamamz.materialgpsapp

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

import android.annotation.SuppressLint
import android.app.Activity

import android.content.*
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.OvershootInterpolator

import android.widget.TextView
import android.widget.Toast
import com.github.clans.fab.FloatingActionMenu
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.yamamz.materialgpsapp.fragment.Location
import com.yamamz.materialgpsapp.fragment.SaveLocationsFragment
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.service.locationService
import java.util.ArrayList
import io.realm.Realm
import kotlinx.android.synthetic.main.tab_layout.*


/**
* Created by Raymundo T. Melecio on 11/30/2016.
*/


class MainActivity : AppCompatActivity() {

    var tabLocation: String? = null
    var tabLocationSave: String? = null
    private var Northing: TextView? = null
    private var Easting: TextView? = null
    private var isFabShowing = true
    private var isFabRefreshShowing = true
    private var LatitudeText: TextView? = null
    private var LongittudeText: TextView? = null
    private var Elevation: TextView? = null
    private var Speed: TextView? = null
    private var AcuracyText: TextView? = null

    var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var menus:ArrayList<FloatingActionMenu>?  = java.util.ArrayList()
    private val mUiHandler = Handler()


    val lat: Double?
        get() = java.lang.Double.parseDouble(LatitudeText?.text.toString())
    val lon: Double?
        get() = java.lang.Double.parseDouble(LongittudeText?.text.toString())
    val easting: Double?
        get() = java.lang.Double.parseDouble(Easting?.text.toString())
    val northing: Double?
        get() = java.lang.Double.parseDouble(Northing?.text.toString())
    val elevation: Double?
        get() = java.lang.Double.parseDouble(Elevation?.text.toString())

    private var locationFragment: Location? = null
    private var saveLocationFragment: SaveLocationsFragment?=null
    private var realm: Realm? = null
    //Broadcast receiver coming from a service that receives each time it will pass an intent
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<android.location.Location>("location")
            Log.e("Yamamz","New Location")
            //execute a function from a Location fragment and pass location to its argument that was ask
            val LocationFragment = this@MainActivity
                    .supportFragmentManager
                    .findFragmentByTag(tabLocation) as Location
                    LocationFragment.setLocations(location.latitude, location.longitude,
                    location.altitude, location.accuracy, location.speed.toDouble())


        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        Realm.init(this)
        realm = Realm.getDefaultInstance()
        initialize()

        menu_track?.hideMenuButton(false)
        menus?.add(menu_track)

        var delay = 400
        menus?.forEach {
            mUiHandler.postDelayed(Runnable { it.showMenuButton(true) }, delay.toLong())
            delay += 150
        }



        createCustomAnimation()

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        if (viewPager != null) {
            setupViewPager(viewPager)
        }
        //initialize tab layout
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)


        val collapsingToolbar = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        collapsingToolbar.title = " "
        val mAppBarLayout = findViewById<AppBarLayout>(R.id.appbar)



        mAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (verticalOffset == -collapsingToolbar.height + toolbar.height) {
                collapsingToolbar.title = "GPS"
            } else {

                collapsingToolbar.title = " "
            }
        }

        fab_track.setOnClickListener { view ->
            //for adding locations to fragment
            locationFragment = this@MainActivity
                    .supportFragmentManager
                    .findFragmentByTag(tabLocation) as Location
            locationFragment?.addLocation()

        }

        fab_refresh.setOnClickListener {
            val saveFagment = this@MainActivity
                    .supportFragmentManager
                    .findFragmentByTag(tabLocationSave) as SaveLocationsFragment
            saveFagment.loadlocationsDatabase()
        }
        //for getting gps service status
        val manager = this@MainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //check if the device has gps support
        if (!hasGPSDevice(this@MainActivity)) {
            Toast.makeText(this@MainActivity, "Gps not Supported", Toast.LENGTH_SHORT).show()
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(this@MainActivity)) {
            //request permission for location
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                //enable gps
                enableLoc()
                //start the background service to get location updates
                startService(Intent(this@MainActivity, locationService::class.java))

            }

        } else {

            if (!checkPermissions()) {
                requestPermissions()
            } else {
                 startService(Intent(this@MainActivity, locationService::class.java))
            }
        }


        }


    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = Adapter(supportFragmentManager)
        adapter.addFragment(Location(), "Get Location")
        adapter.addFragment(SaveLocationsFragment(), "Save locations")
        viewPager.adapter = adapter

    }

    fun createCustomAnimation() {
        val set = AnimatorSet()
        val scaleOutX = ObjectAnimator.ofFloat(menu_track.menuIconView, "scaleX", 1.0f, 0.2f)
        val scaleOutY = ObjectAnimator.ofFloat(menu_track.menuIconView, "scaleY", 1.0f, 0.2f)
        val scaleInX = ObjectAnimator.ofFloat(menu_track.menuIconView, "scaleX", 0.2f, 1.0f)
        val scaleInY = ObjectAnimator.ofFloat(menu_track.menuIconView, "scaleY", 0.2f, 1.0f)

        scaleOutX.duration = 50
        scaleOutY.duration = 50
        scaleInX.duration = 150
        scaleInY.duration = 150

        scaleInX.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                menu_track?.menuIconView?.setImageResource(if (menu_track.isOpened)
                    R.drawable.ic_close_white_24dp
                else
                    R.drawable.fab_add)
            }
        })

        set.play(scaleOutX).with(scaleOutY)
        set.play(scaleInX).with(scaleInY).after(scaleOutX)
        set.interpolator = OvershootInterpolator(2f)
        menu_track.iconToggleAnimatorSet = set
    }


    internal fun initialize() {

        Northing = findViewById(R.id.longitude)
        Easting = findViewById(R.id.easting)
        LatitudeText = findViewById(R.id.latitude)
        LongittudeText = findViewById(R.id.northing)
        Elevation = findViewById(R.id.elevation)
        AcuracyText = findViewById(R.id.acu)
        Speed = findViewById(R.id.speed)

    }

    private class Adapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private val mFragments = ArrayList<Fragment>()
        private val mFragmentTitles = ArrayList<String>()

        internal fun addFragment(fragment: Fragment, title: String) {
            mFragments.add(fragment)
            mFragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitles[position]
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (item.itemId) {

            R.id.action_settings -> {
            }
            R.id.icnSave -> {
                locationFragment = this@MainActivity
                        .supportFragmentManager
                        .findFragmentByTag(tabLocation) as Location

                locationFragment?.Saveloc()
            }
            R.id.icnRemove -> {
                locationFragment = this@MainActivity
                        .supportFragmentManager
                        .findFragmentByTag(tabLocation) as Location
                locationFragment?.clearLocations()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun setViews(northings: String, easting: String, latitude: String, longitude: String, acuracy: String, elevation: String, speed: String) {

        LatitudeText?.text = latitude
        LongittudeText?.text = longitude
        Easting?.text = easting
        Northing?.text = northings
        Elevation?.text = elevation
        AcuracyText?.text = acuracy
        Speed?.text = speed

    }

    @SuppressLint("ObsoleteSdkInt")
//    fun hideFab() {
//
//        if (isFabShowing && fab != null) {
//            isFabShowing = false
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                val point = Point()
//                this.window.windowManager.defaultDisplay.getSize(point)
//                val translation = fab?.y?.minus(point.y)
//                if (translation != null) {
//                    fab?.animate()?.translationYBy(-translation)?.start()
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
//                        fab?.isClickable = false
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation) {
//
//                    }
//                })
//                fab?.startAnimation(animation)
//
//            }
//
//
//        }
//    }

//    @SuppressLint("ObsoleteSdkInt")
//    fun showFab() {
//
//        if (!isFabShowing && fab != null) {
//            isFabShowing = true
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                fab!!.animate().translationY(0f).start()
//            } else {
//                val animation = AnimationUtils.makeInAnimation(this, false)
//                animation.fillAfter = true
//                animation.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationStart(animation: Animation) {}
//
//                    override fun onAnimationEnd(animation: Animation) {
//                        fab?.isClickable = true
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation) {}
//                })
//                fab?.startAnimation(animation)
//            }
//
//
//        }
//    }
//
//
//    @SuppressLint("ObsoleteSdkInt")
//    fun hideFabRefresh() {
//        Log.e("error" ,"Success")
//        if (isFabRefreshShowing && fabRefresh != null) {
//            isFabRefreshShowing = false
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                val point = Point()
//                this.window.windowManager.defaultDisplay.getSize(point)
//                val translation = fabRefresh?.y?.minus(point.y)
//                if (translation != null) {
//                    fabRefresh?.animate()?.translationYBy(-translation)?.start()
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
//                        fabRefresh?.isClickable = false
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation) {
//
//                    }
//                })
//                fabRefresh?.startAnimation(animation)
//            }
//        }
//    }
//
//    @SuppressLint("ObsoleteSdkInt")
//    fun showFabRefresh() {
//        Log.e("error" ,"Success")
//        if (!isFabRefreshShowing && fabRefresh != null) {
//            isFabRefreshShowing = true
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                fabRefresh?.animate()?.translationY(0f)?.start()
//            } else {
//                val animation = AnimationUtils.makeInAnimation(this, false)
//                animation.fillAfter = true
//                animation.setAnimationListener(object : Animation.AnimationListener {
//                    override fun onAnimationStart(animation: Animation) {}
//
//                    override fun onAnimationEnd(animation: Animation) {
//                        fabRefresh?.isClickable = true
//                    }
//
//                    override fun onAnimationRepeat(animation: Animation) {}
//                })
//                fabRefresh?.startAnimation(animation)
//            }
//        }
//    }



    fun deletelocation(filename: String) {
        val coordinatorLayout = findViewById<DrawerLayout>(R.id
                .drawer_layout)

        val bar = Snackbar.make(coordinatorLayout, "Delete Item", Snackbar.LENGTH_SHORT)
                .setAction("ok") {
                    // Handle user action

                    val realm = Realm.getDefaultInstance()
                    try {

                        realm.executeTransactionAsync(Realm.Transaction { realmAsync ->
                            val results = realmAsync.where(SaveLocation::class.java)
                                    .equalTo("fileName",
                                            filename).findAll()
                            results.deleteAllFromRealm()
                        }, Realm.Transaction.OnSuccess {
                            val saveFagment = this@MainActivity
                                    .supportFragmentManager
                                    .findFragmentByTag(tabLocationSave) as SaveLocationsFragment
                            saveFagment.loadlocationsDatabase()
                        })

                    } catch (ignore: Exception) {

                    } finally {
                        realm.close()
                    }
                }

        bar.show()


    }

    override fun onResume() {
        super.onResume()

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, IntentFilter("getlocation"))

    }
    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
        stopService(Intent(this@MainActivity, locationService::class.java))
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    override fun onPause() {
        super.onPause()
        realm?.close()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
    }

    private fun hasGPSDevice(context: Context): Boolean {
        val mgr = context
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mgr.allProviders ?: return false
        return providers.contains(LocationManager.GPS_PROVIDER)
    }
    private fun enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(this@MainActivity)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(bundle: Bundle?) {

                        }

                        override fun onConnectionSuspended(i: Int) {
                            googleApiClient?.connect()
                        }
                    })
                    .addOnConnectionFailedListener { connectionResult -> Log.d("Location error", "Location error " + connectionResult.errorCode) }.build()
            googleApiClient?.connect()

            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = (30 * 1000).toLong()
            locationRequest.fastestInterval = (5 * 1000).toLong()
            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)


            val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
            result.addOnCompleteListener {
                try {
                    val response = result.getResult(ApiException::class.java)
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (exception: ApiException) {
                    when(exception.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                val resolvable =  exception as ResolvableApiException
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        this@MainActivity,
                                        REQUEST_LOCATION)
                            } catch (e: IntentSender.SendIntentException) {
                                // Ignore the error.
                            } catch (e:ClassCastException ) {
                                // Ignore, should be an impossible error.
                            }

                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->{

                        }
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    }
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val states = LocationSettingsStates.fromIntent(intent)
        when(requestCode) {
            REQUEST_LOCATION ->{
                when (resultCode) {
                    Activity.RESULT_OK -> {

                    }

                    Activity.RESULT_CANCELED ->{


                    }
                }
                // The user was asked to change settings, but chose not to

            }
        }
    }
    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i("YAMAMZ", "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                    findViewById(R.id.drawer_layout),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok) {
                        // Request permission
                        ActivityCompat.requestPermissions(this@MainActivity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_PERMISSIONS_REQUEST_CODE)
                    }
                    .show()
        } else {
            Log.i("Yamamz", "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("Yamamz", "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i("Yamamz", "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                //mService?.requestLocationUpdates()
            } else {
                // Permission denied.
                // setButtonsState(false)
                Snackbar.make(
                        findViewById(R.id.drawer_layout),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
            }
        }


    }


}