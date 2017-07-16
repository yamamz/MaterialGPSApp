package com.yamamz.materialgpsapp.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yamamz.materialgpsapp.MainActivity;
import com.yamamz.materialgpsapp.R;
import com.yamamz.materialgpsapp.model.SaveLocation;
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration;
import com.yamamz.materialgpsapp.ui.saveLocationAdapter;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

/**
 * A simple {@link Fragment} subclass.
 */
public class SaveLocationsFragment extends Fragment {

    private Realm realm;
    private View RootView;
    private RecyclerView recyclerView;
    private saveLocationAdapter mAdapter;
    private List<SaveLocation> saveLocations=new ArrayList<>();
    public SaveLocationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RootView=inflater.inflate(R.layout.fragment_save_locations, container, false);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();

        String myTag=getTag();
        ((MainActivity)getActivity()).setTabLocationSave(myTag);
      //  ((locDetails) getActivity()).setTabLocation(myTag);


        setupRecyclerView();
        loadlocationsDatabase();
        // Inflate the layout for this fragment
        return RootView;
    }
    private void setupRecyclerView() {
        recyclerView = (RecyclerView) RootView.findViewById(R.id.recycleView);
        mAdapter = new saveLocationAdapter(getActivity(),saveLocations);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DeviderItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }



    public void loadlocationsDatabase(){
        if (saveLocations.size()>0){
            saveLocations.clear();
        }
        for (SaveLocation saveLocation : realm.where(SaveLocation.class).findAllSorted("fileName",
                Sort.ASCENDING)) {
            SaveLocation save = new SaveLocation(saveLocation.getFileName(),saveLocation.getArea());
            saveLocations.add(save);
        }
        mAdapter.notifyDataSetChanged();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }


}
