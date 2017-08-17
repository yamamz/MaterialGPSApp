package com.yamamz.materialgpsapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.yamamz.materialgpsapp.MainActivity;
import com.yamamz.materialgpsapp.R;
import com.yamamz.materialgpsapp.model.LocationModel;
import com.yamamz.materialgpsapp.model.SaveLocation;
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration;
import com.yamamz.materialgpsapp.ui.RecyclerItemClickListener;
import com.yamamz.materialgpsapp.ui.locationAdapter;
import com.yamamz.materialgpsapp.utils.CoordinateConversion;
import com.yamamz.materialgpsapp.utils.DecimalToDMS;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

public class Location extends Fragment  {


    private CoordinateConversion convertUtm = new CoordinateConversion();

    private static final int REQUEST_CODE_LOCATION = 2;

    private int mPosition;
    private double EastingFormat;
    private double NorthingFormat;
    private double AreaOfPolygon;
    private double latitude;
    private double longitude;
    private double altitiude;
    private float acuracy;
    private double speed;


    private RealmList<LocationModel> locationList = new RealmList<>();
    ArrayList<Double> Northings = new ArrayList<>();
    ArrayList<Double> Eastings = new ArrayList<>();

    private SaveLocation saveLocation;

    private String lat;
    private String lon;
    private CharSequence filename;

    private RecyclerView recyclerView;
    private locationAdapter mAdapter;


    private LinearLayout emptyTextView;
    private Realm realm;
    private View RootView;



    public Location() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String myTag = getTag();
        ((MainActivity) getActivity()).setTabLocation(myTag);


        RootView = inflater.inflate(R.layout.fragment_location, container, false);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();

        initialize();

        return RootView;
    }

    public void addLocation() {
        if (latitude != 0 && longitude != 0) {
            int positionNorthingEasting = mAdapter.getItemCount();
            LocationModel LocationList = new LocationModel(((MainActivity) getActivity()).getLat(),
                    ((MainActivity) getActivity()).getLon(), String.valueOf(mAdapter.getItemCount()), ((MainActivity) getActivity()).getElevation());
            locationList.add(LocationList);
            mAdapter.notifyItemInserted(locationList.size());
            Northings.add(positionNorthingEasting, ((MainActivity) getActivity()).getNorthing());
            Eastings.add(positionNorthingEasting, ((MainActivity) getActivity()).getEasting());


            if (mAdapter.getItemCount() >= 3) {
                calculateArea();

            }
        }


    }


    void initialize() {

        emptyTextView = (LinearLayout) RootView.findViewById(R.id.empty);
        recyclerView = (RecyclerView) RootView.findViewById(R.id.recyclerView);
        mAdapter = new locationAdapter(locationList, getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DeviderItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);


        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener
                        .OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {

                        mPosition = position;

                        final boolean[] clickOK = {false};
                        Snackbar bar = Snackbar.make(view, "Delete Item", Snackbar.LENGTH_SHORT)
                                .setAction("ok", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Handle user action
                                        try {
                                            locationList.remove(mPosition);
                                            mAdapter.notifyItemRemoved(mPosition);
                                            mAdapter.notifyItemRangeChanged(0, locationList.size());
                                            Northings.remove(mPosition);
                                            Eastings.remove(mPosition);
                                            calculateArea();
                                        } catch (Exception ignore) {

                                        }
                                    }


                                });

                        bar.show();

                        if (clickOK[0]) {

                        }
                    }
                }));

        checkAdapter();


    }






    public void clearLocations() {

        locationList.clear();
        mAdapter.notifyDataSetChanged();
        AreaOfPolygon = 0.00;

    }



    void calculateArea() {
        if (Northings.size() >= 3) {
            double sum = 0;
            double area;
            double[] prodx = new double[Northings.size()];
            double[] prody = new double[Northings.size()];
            double[] sumxy = new double[Northings.size()];
            for (int iteration = 0; iteration < Northings.size(); iteration++) {
                if (iteration < Northings.size() - 1) {
                    prodx[iteration] = Northings.get(iteration) * Eastings.get(iteration + 1);
                    prody[iteration] = Eastings.get(iteration) * Northings.get(iteration + 1);
                }
                if (iteration == Northings.size() - 1) {
                    prodx[iteration] = Northings.get(iteration) * Eastings.get(0);
                    prody[iteration] = Eastings.get(iteration) * Northings.get(0);
                }
                sumxy[iteration] = prodx[iteration] - prody[iteration];

            }


            for (double l : sumxy) {
                sum += l;

            }

            if (sum < 0) {
                sum *= -1;
            } else {
                sum *= 1;
            }

            area = sum / 2;

            AreaOfPolygon = area;


        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();


    }


    public void Saveloc() {


        if (locationList.size() >= 1)
            new MaterialDialog.Builder(getActivity()).title(R.string.input).inputType(InputType.TYPE_CLASS_TEXT).input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            filename = input;
                            save();
                        }
                    }

            ).show();
    }

    void save() {
        Double area = AreaOfPolygon;
        saveLocation = new SaveLocation(filename.toString(), locationList, area);
        SaveTolocal();
    }

    void SaveTolocal() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                try {
                    bgRealm.copyToRealm(saveLocation);
                } catch (Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "your filename is already exist", Toast
                                    .LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "Save Successfully", Toast.LENGTH_SHORT).show();
                addlocation();
                locationList.clear();
                mAdapter.notifyDataSetChanged();
                // Answer.setText("0.00");

            }
        });
    }

    void addlocation() {
        String TabOfFragmentAnswer = ((MainActivity) getActivity()).getTabLocationSave();
        SaveLocationsFragment LocationSaveFragment = (SaveLocationsFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragmentAnswer);
        LocationSaveFragment.loadlocationsDatabase();

    }

    public void setLocations(double lati, double longi, double al, float acu,double sp) {

        latitude = lati;
        longitude = longi;
        acuracy = acu;
        altitiude = al;
        speed = sp;


            //stopProgressBar();
            String UTM = convertUtm.latLon2UTM(latitude, longitude);
            int lastdot = UTM.lastIndexOf("-");
            String E = UTM.substring(0, lastdot);
            String N = UTM.substring(lastdot + 1, UTM.length());
            EastingFormat = Double.parseDouble(E);
            NorthingFormat = Double.parseDouble(N);
            EastingFormat = Math.round(EastingFormat * 10000.0) / 10000.0;
            NorthingFormat = Math.round(NorthingFormat * 10000.0) / 10000.0;
            //convert degress minutes seconds from WGS84
            DecimalToDMS ConverterDMS = new DecimalToDMS();
            lat = ConverterDMS.decimalToDMS(latitude);
            lon = ConverterDMS.decimalToDMS(longitude);


            DecimalFormat df = new DecimalFormat("###.##");
            double convertToKM = speed * 1.3;
        if(latitude!=0 || longitude !=0)
            ((MainActivity) getActivity()).setViews(String.valueOf(NorthingFormat), String.valueOf(EastingFormat), String.valueOf(latitude), String.valueOf(longitude), String.valueOf(acuracy), String.valueOf(altitiude), df.format(convertToKM) + " " + "KM/H");


        }




    private void checkRecyclerViewIsemplty(){
        if(mAdapter.getItemCount()==0){

            emptyTextView.setVisibility(View.VISIBLE);
        }
        else{

            emptyTextView.setVisibility(View.GONE);
        }


    }


    void checkAdapter(){
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkRecyclerViewIsemplty();
            }
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                checkRecyclerViewIsemplty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkRecyclerViewIsemplty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkRecyclerViewIsemplty();
            }
        });

    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

try{
           ((MainActivity)getActivity()).showFab();
}
catch (Exception ignore){}
        }
        else {
      try{
            ((MainActivity)getActivity()).hideFab();
      }
      catch (Exception ignore){}
        }
    }

        }
