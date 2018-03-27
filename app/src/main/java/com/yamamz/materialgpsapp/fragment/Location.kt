package com.yamamz.materialgpsapp.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.model.LatLng
import com.yamamz.materialgpsapp.MainActivity
import com.yamamz.materialgpsapp.R
import com.yamamz.materialgpsapp.model.LocationModel
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration
import com.yamamz.materialgpsapp.ui.RecyclerItemClickListener
import com.yamamz.materialgpsapp.ui.locationAdapter
import com.yamamz.materialgpsapp.utils.CoordinateConversion
import com.yamamz.materialgpsapp.utils.DecimalToDMS

import java.text.DecimalFormat

import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_plot.*
import kotlinx.android.synthetic.main.fragment_location.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
* Created by Raymundo T. Melecio on 11/30/2016.
*/

class Location : Fragment() {


    private val convertUtm = CoordinateConversion()

    private var mPosition: Int = 0
    private var EastingFormat: Double = 0.toDouble()
    private var NorthingFormat: Double = 0.toDouble()
    private var AreaOfPolygon: Double = 0.toDouble()
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var altitiude: Double = 0.toDouble()
    private var acuracy: Float = 0.toFloat()
    private var speed: Double = 0.toDouble()
    private val locationList = RealmList<LocationModel>()
    internal var Northings = ArrayList<Double>()
    internal var Eastings = ArrayList<Double>()

    private var saveLocation: SaveLocation? = null

    private var lat: String? = null
    private var lon: String? = null
    private var filename: CharSequence? = null

    private var recyclerView: RecyclerView? = null
    private var mAdapter: locationAdapter? = null


    private var emptyTextView: LinearLayout? = null
    private var realm: Realm? = null
    private var RootView: View? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val myTag = tag
        (activity as MainActivity).tabLocation = myTag


        RootView = inflater?.inflate(R.layout.fragment_location, container, false)


        realm = Realm.getDefaultInstance()

        initialize()

        return RootView
    }

    fun addLocation() {
        if (latitude != 0.0 && longitude != 0.0) {
            val positionNorthingEasting = mAdapter!!.itemCount
            val LocationList = LocationModel((activity as MainActivity).lat,
                    (activity as MainActivity).lon, mAdapter?.itemCount.toString(), (activity as MainActivity).elevation)
            locationList.add(LocationList)
            mAdapter?.notifyItemInserted(locationList.size)
            (activity as MainActivity).northing?.let { Northings.add(positionNorthingEasting, it) }
            (activity as MainActivity).easting?.let { Eastings.add(positionNorthingEasting, it) }


            if (mAdapter?.itemCount?:0 >= 3) {
                calculateArea()

            }
        }


    }


    internal fun initialize() {

        emptyTextView = RootView?.findViewById(R.id.empty)
        recyclerView = RootView?.findViewById(R.id.recyclerView)
        mAdapter = locationAdapter(locationList, activity)
        val mLayoutManager = LinearLayoutManager(recyclerView?.context)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.addItemDecoration(DeviderItemDecoration(activity, LinearLayoutManager.VERTICAL))
        recyclerView?.adapter = mAdapter


        recyclerView?.addOnItemTouchListener(
                RecyclerItemClickListener(activity, RecyclerItemClickListener.OnItemClickListener { view, position ->
                    mPosition = position

                    val clickOK = booleanArrayOf(false)
                    val bar = Snackbar.make(view, "Delete Item", Snackbar.LENGTH_SHORT)
                            .setAction("ok") {
                                // Handle user action
                                try {
                                    locationList.removeAt(mPosition)
                                    mAdapter?.notifyItemRemoved(mPosition)
                                    mAdapter?.notifyItemRangeChanged(0, locationList.size)
                                    Northings.removeAt(mPosition)
                                    Eastings.removeAt(mPosition)
                                    calculateArea()
                                } catch (ignore: Exception) {

                                }
                            }

                    bar.show()

                    if (clickOK[0]) {

                    }
                }))

        checkAdapter()


    }


    fun clearLocations() {

        locationList.clear()
        mAdapter?.notifyDataSetChanged()
        AreaOfPolygon = 0.00

    }


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


    override fun onDestroy() {
        super.onDestroy()
    }


    fun Saveloc() {


        if (locationList.size >= 1)
            MaterialDialog.Builder(activity).title(R.string.input).inputType(InputType.TYPE_CLASS_TEXT).input(R.string.input_hint, R.string.input_prefill

            ) { dialog, input ->
                filename = input

                if (filename?.length?:0 > 0) {
                    save()
                }

            }.show()
    }

    internal fun save() {
        val area = AreaOfPolygon
        saveLocation = SaveLocation(filename?.toString(), locationList, area)
        val realm = Realm.getDefaultInstance()
        try {
            realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                try {
                    bgRealm.copyToRealm(saveLocation)
                } catch (e: Exception) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "your filename is already exist", Toast
                                .LENGTH_SHORT)
                                .show()
                    }
                }
            }, Realm.Transaction.OnSuccess {

                val dateFormat= SimpleDateFormat("yyyy-MM-dd")
                val now= Date()
                val file_name=dateFormat.format(now)
                val loc_list=ArrayList<LatLng>()
                locationList.forEach {
                    loc_list.add(LatLng(it.latitude,it.longitude))
                }
                generateCSVOnSD(filename.toString().plus(file_name.plus("WGS_84.csv")),loc_list)
                addlocation()
                locationList.clear()
                mAdapter?.notifyDataSetChanged()
                // Answer.setText("0.00");
            })

        } catch (ignored: Exception) {
        } finally {
            realm.close()
        }
    }


    fun generateCSVOnSD(sFileName:String, sBody:ArrayList<LatLng>? ) {

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
            Snackbar.make(f_location, "Saved and exported csv to".plus(root.absolutePath), Snackbar.LENGTH_LONG)
                    .setAction("OK", {

                    })
                    .setActionTextColor(Color.RED)
                    .show()

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    internal fun addlocation() {
        val TabOfFragmentAnswer = (activity as MainActivity).tabLocationSave
        val LocationSaveFragment = activity
                .supportFragmentManager
                .findFragmentByTag(TabOfFragmentAnswer) as SaveLocationsFragment
        LocationSaveFragment.loadlocationsDatabase()

    }

    fun setLocations(lati: Double, longi: Double, al: Double, acu: Float, sp: Double) {

        latitude = lati
        longitude = longi
        acuracy = acu
        altitiude = al
        speed = sp


        val UTM = convertUtm.latLon2UTM(latitude, longitude)
        val lastdot = UTM.lastIndexOf("-")
        val E = UTM.substring(0, lastdot)
        val N = UTM.substring(lastdot + 1, UTM.length)
        EastingFormat = java.lang.Double.parseDouble(E)
        NorthingFormat = java.lang.Double.parseDouble(N)
        EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0
        NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0

        //convert degress minutes seconds from WGS84
        val ConverterDMS = DecimalToDMS()
        lat = ConverterDMS.decimalToDMS(latitude)
        lon = ConverterDMS.decimalToDMS(longitude)


        val df = DecimalFormat("###.##")
        val convertToKM = speed * 1.3
        if (latitude != 0.0 || longitude != 0.0)
            (activity as MainActivity).setViews(NorthingFormat.toString(), EastingFormat.toString(), latitude.toString(), longitude.toString(), acuracy.toString(), altitiude.toString(), df.format(convertToKM) + " " + "KM/H")


    }


    private fun checkRecyclerViewIsemplty() {
        if (mAdapter!!.itemCount == 0) {

            emptyTextView?.visibility = View.VISIBLE
        } else {

            emptyTextView?.visibility = View.GONE
        }


    }


    internal fun checkAdapter() {
        mAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkRecyclerViewIsemplty()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                checkRecyclerViewIsemplty()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkRecyclerViewIsemplty()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkRecyclerViewIsemplty()
            }
        })

    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {

            try {
                (activity as MainActivity).showFab()
                (activity as MainActivity).hideFabRefresh()
            } catch (e: Exception) {

                Log.e("error" ,e.message)
            }

        } else {
            try {
                (activity as MainActivity).hideFab()
               (activity as MainActivity).showFabRefresh()
            } catch (e: Exception) {
                Log.e("error" ,e.message)
            }

        }
    }

    companion object {

        private val REQUEST_CODE_LOCATION = 2
    }

}// Required empty public constructor
