package com.yamamz.materialgpsapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Point
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast

import com.yamamz.materialgpsapp.fragment.Location
import com.yamamz.materialgpsapp.fragment.SaveLocationsFragment
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.service.locationService

import java.util.ArrayList

import io.realm.Realm
import io.realm.RealmResults


class MainActivity : AppCompatActivity() {

    var tabLocation: String? = null
    var tabLocationSave: String? = null


    private var pDialog: ProgressDialog? = null
    private var Northing: TextView? = null
    private var Easting: TextView? = null
    private var isFabShowing = true
    private var LatitudeText: TextView? = null
    private var LongittudeText: TextView? = null
    private var Elevation: TextView? = null
    private var Speed: TextView? = null
    private var AcuracyText: TextView? = null

    private var fab: FloatingActionButton? = null


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

    private var InputFragment: Location? = null
    private var realm: Realm? = null

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<android.location.Location>("location")

            val LocationFragment = this@MainActivity
                    .supportFragmentManager
                    .findFragmentByTag(tabLocation) as Location
                    LocationFragment.setLocations(location.latitude, location.longitude,
                    location.altitude, location.accuracy, location.speed.toDouble())


        }
    }

    private val mMessageReceiver1 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(this@MainActivity, "your location updates is not running please enable the gps", Toast.LENGTH_SHORT).show()


        }
    }

    private val mMessageReceiver2 = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            startProgressBar()


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

        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        if (viewPager != null) {
            setupViewPager(viewPager)
        }

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




        fab?.setOnClickListener { view ->

            InputFragment = this@MainActivity
                    .supportFragmentManager
                    .findFragmentByTag(tabLocation) as Location
            InputFragment?.addLocation()

        }

        startService(Intent(this@MainActivity, locationService::class.java))
        pDialog = ProgressDialog(this)


        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            askUserToOpenGPS()
        } else {
            startProgressBar()
        }


    }


    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = Adapter(supportFragmentManager)
        adapter.addFragment(Location(), "Get Location")
        adapter.addFragment(SaveLocationsFragment(), "Save locations")
        viewPager.adapter = adapter

    }


    internal fun initialize() {
        fab = findViewById(R.id.fab)
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
                InputFragment = this@MainActivity
                        .supportFragmentManager
                        .findFragmentByTag(tabLocation) as Location

                InputFragment?.Saveloc()
            }
            R.id.icnRemove -> {
                InputFragment = this@MainActivity
                        .supportFragmentManager
                        .findFragmentByTag(tabLocation) as Location
                InputFragment?.clearLocations()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun setViews(northings: String, easting: String, latitude: String, longitude: String, acuracy: String, elevation: String, speed: String) {
        stopProgressBar()
        LatitudeText?.text = latitude
        LongittudeText?.text = longitude
        Easting?.text = easting
        Northing?.text = northings
        Elevation?.text = elevation
        AcuracyText?.text = acuracy
        Speed?.text = speed

    }

    @SuppressLint("ObsoleteSdkInt")
    fun hideFab() {
        if (isFabShowing && fab != null) {
            isFabShowing = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                val point = Point()
                this.window.windowManager.defaultDisplay.getSize(point)
                val translation = fab?.y?.minus(point.y)
                if (translation != null) {
                    fab?.animate()?.translationYBy(-translation)?.start()
                }
            } else {
                val animation = AnimationUtils.makeOutAnimation(this, true)
                animation.fillAfter = true
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        fab?.isClickable = false
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
        if (!isFabShowing && fab != null) {
            isFabShowing = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                fab!!.animate().translationY(0f).start()
            } else {
                val animation = AnimationUtils.makeInAnimation(this, false)
                animation.fillAfter = true
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        fab?.isClickable = true
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                fab?.startAnimation(animation)
            }
        }
    }


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



        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver1, IntentFilter("disableGps"))

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver2, IntentFilter("openpregress"))
    }

    fun startProgressBar() {

        pDialog?.setMessage("Please wait...")
        pDialog?.setCancelable(true)
        pDialog?.show()
    }

    fun stopProgressBar() {
        if (pDialog?.isShowing==true)
            pDialog?.dismiss()
    }

    fun askUserToOpenGPS() {
        val mAlertDialog = AlertDialog.Builder(this)

        // Setting Dialog Title
        mAlertDialog.setTitle("Location not available, Open GPS?")
                .setMessage("Activate GPS to use use location service and restart the app")
                .setPositiveButton("Open Settings") { dialog, which ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
        stopService(Intent(this@MainActivity, locationService::class.java))
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver1)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver2)
    }

    override fun onPause() {
        super.onPause()
        realm?.close()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver1)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver2)
    }


}