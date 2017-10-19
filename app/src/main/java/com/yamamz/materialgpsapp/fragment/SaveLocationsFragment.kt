package com.yamamz.materialgpsapp.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.yamamz.materialgpsapp.MainActivity
import com.yamamz.materialgpsapp.R
import com.yamamz.materialgpsapp.model.SaveLocation
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration
import com.yamamz.materialgpsapp.ui.saveLocationAdapter

import java.util.ArrayList

import io.realm.Realm
import io.realm.Sort

/**
* Created by Raymundo T. Melecio on 11/30/2016.
*/
class SaveLocationsFragment : Fragment() {

    private var realm: Realm? = null
    private var RootView: View? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: saveLocationAdapter? = null
    private val saveLocations = ArrayList<SaveLocation>()


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        RootView = inflater?.inflate(R.layout.fragment_save_locations, container, false)


        realm = Realm.getDefaultInstance()

        val myTag = tag
        (activity as MainActivity).tabLocationSave = myTag
        //


        setupRecyclerView()
        loadlocationsDatabase()
        // Inflate the layout for this fragment
        return RootView
    }

    private fun setupRecyclerView() {
        recyclerView = RootView?.findViewById<View>(R.id.recycleView) as RecyclerView
        mAdapter = saveLocationAdapter(activity, saveLocations)
        val mLayoutManager = LinearLayoutManager(recyclerView!!.context)
        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.addItemDecoration(DeviderItemDecoration(activity, LinearLayoutManager.VERTICAL))
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = mAdapter
    }


    fun loadlocationsDatabase() {
        if (saveLocations.size > 0) {
            saveLocations.clear()
        }
        val realm = Realm.getDefaultInstance()
        try {
            for (saveLocation in realm.where(SaveLocation::class.java).findAllSortedAsync("fileName",
                    Sort.ASCENDING)) {
                val save = SaveLocation(saveLocation.fileName, saveLocation.area)
                saveLocations.add(save)
            }
        } catch (ignored: Exception) {
        } finally {
            realm.close()
            mAdapter?.notifyDataSetChanged()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }


}// Required empty public constructor
