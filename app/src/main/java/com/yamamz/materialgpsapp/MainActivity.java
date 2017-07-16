package com.yamamz.materialgpsapp;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.yamamz.materialgpsapp.fragment.Location;
import com.yamamz.materialgpsapp.fragment.SaveLocationsFragment;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  {

    private String TabLocation;
    private String TabLocationSave;



    private TextView Northing;
    private TextView Easting;
    private boolean isFabShowing = true;
    private TextView LatitudeText;
    private TextView LongittudeText;
    private TextView Elevation;
    private TextView Speed;
    private TextView AcuracyText;

    private FloatingActionButton fab;


    public Double getLat() {
        return Double.parseDouble(LatitudeText.getText().toString());
    }

    public Double getLon() {
        return Double.parseDouble(LongittudeText.getText().toString());
    }

    public Double getEasting() {
        return Double.parseDouble(Easting.getText().toString());
    }

    public Double getNorthing() {
        return Double.parseDouble(Northing.getText().toString());
    }




    public Double getElevation() {
        return Double.parseDouble(Elevation.getText().toString());
    }




    public String getTabLocationSave() {
        return TabLocationSave;
    }

    public void setTabLocationSave(String tabLocationSave) {
        TabLocationSave = tabLocationSave;
    }

    private Location InputFragment;

    public String getTabLocation() {
        return TabLocation;
    }

    public void setTabLocation(String tabLocation) {
        TabLocation = tabLocation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initialize();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout mAppBarLayout=(AppBarLayout) findViewById(R.id.appbar);

        mAppBarLayout.addOnOffsetChangedListener(new   AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == -collapsingToolbar.getHeight() + toolbar.getHeight()) {
                    collapsingToolbar.setTitle("GPS");
                }
                else{

                    collapsingToolbar.setTitle(" ");
                }
            }
        });




        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputFragment=(Location)MainActivity.this
                        .getSupportFragmentManager()
                        .findFragmentByTag(getTabLocation());
                          InputFragment.addLocation();

            }
        });



    }



    private void setupViewPager(ViewPager viewPager) {

        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new Location(), "Get Location");
        adapter.addFragment(new SaveLocationsFragment(), "Save locations");
        viewPager.setAdapter(adapter);

    }



    void initialize() {
        fab=(FloatingActionButton) findViewById(R.id.fab);
        Northing = (TextView) findViewById(R.id.longitude);
        Easting = (TextView) findViewById(R.id.easting);
        LatitudeText = (TextView) findViewById(R.id.latitude);
        LongittudeText = (TextView)findViewById(R.id.northing);
        Elevation = (TextView) findViewById(R.id.elevation);
        AcuracyText = (TextView) findViewById(R.id.acu);
        Speed = (TextView) findViewById(R.id.speed);
    }
    private static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        Adapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        switch (item.getItemId()){

            case R.id.action_settings:

                break;
            case  R.id.icnSave:
                InputFragment=(Location)MainActivity.this
                              .getSupportFragmentManager()
                              .findFragmentByTag(getTabLocation());

                InputFragment.Saveloc();
                break;
            case R.id.icnRemove:
                         InputFragment=(Location)MainActivity.this
                        .getSupportFragmentManager()
                        .findFragmentByTag(getTabLocation());
                         InputFragment.clearLocations();

                break;

        }

        return super.onOptionsItemSelected(item);
    }

public void setViews(String northings,String easting,String latitude,String longitude,String acuracy,String elevation, String speed){

    LatitudeText.setText(latitude);
    LongittudeText.setText(longitude);
   Easting.setText(easting);
   Northing.setText(northings);
   Elevation.setText(elevation);
    AcuracyText.setText(acuracy);
    Speed.setText(speed);

}

    @SuppressLint("ObsoleteSdkInt")
    public void hideFab() {
        if (isFabShowing && fab!=null) {
            isFabShowing = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                final Point point = new Point();
                this.getWindow().getWindowManager().getDefaultDisplay().getSize(point);
                final float translation = fab.getY() - point.y;
                fab.animate().translationYBy(-translation).start();
            } else {
                Animation animation = AnimationUtils.makeOutAnimation(this, true);
                animation.setFillAfter(true);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fab.setClickable(false);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fab.startAnimation(animation);
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    public void showFab() {
        if (!isFabShowing && fab!=null) {
            isFabShowing = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                fab.animate().translationY(0).start();
            } else {
                Animation animation = AnimationUtils.makeInAnimation(this, false);
                animation.setFillAfter(true);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fab.setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                fab.startAnimation(animation);
            }
        }
    }

}