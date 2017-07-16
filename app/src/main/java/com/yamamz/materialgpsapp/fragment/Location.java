package com.yamamz.materialgpsapp.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.yamamz.materialgpsapp.thread.MyWorkerThread;
import com.yamamz.materialgpsapp.ui.DeviderItemDecoration;
import com.yamamz.materialgpsapp.ui.RecyclerItemClickListener;
import com.yamamz.materialgpsapp.ui.locationAdapter;
import com.yamamz.materialgpsapp.utils.CoordinateConversion;
import com.yamamz.materialgpsapp.utils.DecimalToDMS;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;

import static android.content.Context.LOCATION_SERVICE;

public class Location extends Fragment implements LocationListener {


    private CoordinateConversion convertUtm = new CoordinateConversion();

    private static final int REQUEST_CODE_LOCATION = 2;

    private int mPosition;
    private double EastingFormat;
    private double NorthingFormat;
    private double AreaOfPolygon;
    private double latitude;
    private double longitude;
    private double altitiude;
    private double acuracy;
    private double speed;
    private ProgressDialog pDialog;

    private RealmList<LocationModel> locationList = new RealmList<>();
    ArrayList<Double> Northings = new ArrayList<>();
    ArrayList<Double> Eastings = new ArrayList<>();

    private SaveLocation saveLocation;

    private String lat;
    private String lon;
    private CharSequence filename;

    private RecyclerView recyclerView;
    private locationAdapter mAdapter;
    private Handler mUiHandler = new Handler();
    private MyWorkerThread mWorkerThread;
    private LinearLayout emptyTextView;
    private Realm realm;
    private View RootView;


    // if GPS is enabled
    boolean isGPSEnabled = false;
    // if Network is enabled
    boolean isNetworkEnabled = false;
    // if Location co-ordinates are available using GPS or Network
    public boolean isLocationAvailable = false;
    // Location and co-ordinates coordinates
    android.location.Location mLocation;
    double mLatitude;
    double mLongitude;
    double mAltitude;
    double mSpeed;
    double mAcuracy;
    float mBearing;
    // Minimum time fluctuation for next update (in milliseconds)
    private static final long TIME = 2000;
    // Minimum distance fluctuation for next update (in meters)
    private static final long DISTANCE = 0;
    // Declaring a Location Manager
    protected LocationManager mLocationManager;

     public Location() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        String myTag = getTag();
        ((MainActivity) getActivity()).setTabLocation(myTag);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        RootView = inflater.inflate(R.layout.fragment_location, container, false);

        Realm.init(getActivity());
        realm = Realm.getDefaultInstance();

        initialize();
        getLocation();

        return RootView;
    }

    public void addLocation() {
        if (latitude!=0 && longitude!=0) {
            int positionNorthingEasting = mAdapter.getItemCount();
            LocationModel LocationList = new LocationModel(((MainActivity)getActivity()).getLat(),
                    ((MainActivity)getActivity()).getLon(), String.valueOf(mAdapter.getItemCount()),((MainActivity)getActivity()).getElevation());
            locationList.add(LocationList);
            mAdapter.notifyItemInserted(locationList.size());
            Northings.add(positionNorthingEasting, ((MainActivity)getActivity()).getNorthing());
            Eastings.add(positionNorthingEasting, ((MainActivity)getActivity()).getEasting());


            if (mAdapter.getItemCount() >= 3) {
                calculateArea();

            }
        }


    }

    @Override
    public void onLocationChanged(final android.location.Location location) {


        Thread thread = new Thread() {
            @Override
            public void run() {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                mAltitude = location.getAltitude();
                mAcuracy = location.getAccuracy();
                mSpeed = location.getSpeed();
                mBearing = location.getBearing();
            }
        };
        thread.start();
        GetLocationThread();

        System.gc();

    }

    void initialize() {

        emptyTextView=(LinearLayout) RootView.findViewById(R.id.empty);
        recyclerView = (RecyclerView) RootView.findViewById(R.id.recyclerView);
        mAdapter = new locationAdapter(locationList,getActivity());
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
                                        }catch (Exception ignore){

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

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {


        getlocation();
        GetLocationThread();

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public android.location.Location getlocation() {
        try {

            // Getting GPS status
            isGPSEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // If GPS enabled, get latitude/longitude using GPS Services
            if (isGPSEnabled) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, TIME, DISTANCE, this);
                if (mLocationManager != null) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (mLocation != null) {
                        mLatitude = mLocation.getLatitude();
                        mLongitude = mLocation.getLongitude();
                        isLocationAvailable = true; // setting a flag that
                        // location is available
                        return mLocation;
                    }
                }
            }

            // If we are reaching this part, it means GPS was not able to fetch
            // any location
            // Getting network status
            isNetworkEnabled = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isNetworkEnabled) {

                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, TIME, DISTANCE, this);
                if (mLocationManager != null) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
                            .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission
                                    .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    }
                    mLocation = mLocationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (mLocation != null) {
                        mLatitude = mLocation.getLatitude();
                        mLongitude = mLocation.getLongitude();
                        isLocationAvailable = true; // setting a flag that
                        // location is available
                        return mLocation;
                    }
                }
            }
            // If reaching here means, we were not able to get location neither
            // from GPS not Network,
            if (!isGPSEnabled) {
                // so asking user to open GPS

                Toast.makeText(getActivity(), "Please Enable your GPS",
                        Toast.LENGTH_LONG).show();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // if reaching here means, location was not available, so setting the
        // flag as false
        isLocationAvailable = false;
        return null;
    }

    public void closeGPS() {
        if (mLocationManager != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                   nt[] grantResults)
                // to handle the case w                       ihere the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.removeUpdates(this);
        }

    }

    public void GetLocationThread() {

        if (getActivity() != null) {
            mWorkerThread = new MyWorkerThread("myWorkerThread");
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    latitude = mLatitude;
                    longitude = mLongitude;
                    acuracy = mAcuracy;
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
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mLatitude == 0 || mLongitude == 0) {

                            } else {
                                altitiude = mAltitude;
                                speed = mSpeed;


                                DecimalFormat df = new DecimalFormat("###.##");
                                double convertToKM = speed * 1.3;
                                ((MainActivity)getActivity()).setViews(String.valueOf(NorthingFormat),String.valueOf(EastingFormat),String.valueOf(latitude),String.valueOf(longitude),String.valueOf(acuracy),String.valueOf(altitiude),df.format(convertToKM) + " " + "KM/H");
                            }
                        }
                    });
                    if (mLatitude != 0 || mLongitude != 0) {
                           stopProgressBar();

                    }
                }
            };

            mWorkerThread.start();
            mWorkerThread.prepareHandler();
            mWorkerThread.postTask(task);
            mWorkerThread.postTask(task);
        }
    }

    public void clearLocations(){

        locationList.clear();
        mAdapter.notifyDataSetChanged();
        AreaOfPolygon=0.00;

    }

    public void startProgressBar() {
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void stopProgressBar() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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

    private boolean isGPSEnable() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService
                (LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void askUserToOpenGPS() {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        mAlertDialog.setTitle("Location not available, Open GPS?")
                .setMessage("Activate GPS to use use location service and restart the app")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    void getLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // Request missing location permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);

        } else {

            if (isGPSEnable()) {
                startProgressBar();
                getlocation();
                GetLocationThread();

            } else {
                askUserToOpenGPS();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
     closeGPS();
        getActivity().unregisterReceiver(mGpsSwitchStateReceiver);
    }


    public void Saveloc() {


    if(locationList.size()>=1)
            new MaterialDialog.Builder(getActivity()).title(R.string.input).inputType(InputType.TYPE_CLASS_TEXT).input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
        @Override
        public void onInput (@NonNull MaterialDialog dialog, CharSequence input){
            filename = input;
            save();
        }
    }

    ).show();
}
    void save(){
        Double area= AreaOfPolygon;
        saveLocation = new SaveLocation(filename.toString(),locationList,area);
        SaveTolocal();
        }
        void SaveTolocal(){
        realm.executeTransactionAsync(new Realm.Transaction() {
        @Override
        public void execute(Realm bgRealm) {
        try {
        bgRealm.copyToRealm(saveLocation);
        }
        catch(Exception e){
        getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run(){
        Toast.makeText(getActivity(),"your filename is already exist",Toast
        .LENGTH_SHORT)
        .show();}});}}},new Realm.Transaction.OnSuccess() {
        @Override
        public void onSuccess() {
        Toast.makeText(getActivity(),"Save Successfully",Toast.LENGTH_SHORT).show();
        addlocation();
        locationList.clear();
        mAdapter.notifyDataSetChanged();
       // Answer.setText("0.00");

        }
        });
        }

        void addlocation(){
        String TabOfFragmentAnswer=((MainActivity)getActivity()).getTabLocationSave();
        SaveLocationsFragment LocationSaveFragment=(SaveLocationsFragment) getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(TabOfFragmentAnswer);
                 LocationSaveFragment.loadlocationsDatabase();

        }


    private void checkRecyclerViewIsemplty(){
        if(mAdapter.getItemCount()==0){

            emptyTextView.setVisibility(View.VISIBLE);
        }
        else{

            emptyTextView.setVisibility(View.GONE);
        }


    }

    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
               getLocation();
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
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
