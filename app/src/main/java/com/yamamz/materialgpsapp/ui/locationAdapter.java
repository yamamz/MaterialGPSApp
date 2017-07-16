package com.yamamz.materialgpsapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yamamz.materialgpsapp.R;
import com.yamamz.materialgpsapp.model.LocationModel;
import com.yamamz.materialgpsapp.utils.DecimalToDMS;

import java.util.List;

/**
 * Created by AMRI on 10/3/2016.
 */

public class locationAdapter extends RecyclerView.Adapter<locationAdapter.MyViewHolder> {

    private List<LocationModel> locationList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView northing, count, easthing,avatar;

        public MyViewHolder(View view) {
            super(view);
            northing = (TextView) view.findViewById(R.id.northingList);
            easthing = (TextView) view.findViewById(R.id.eastingList);
            count = (TextView) view.findViewById(R.id.position);
            avatar = (TextView) view.findViewById(R.id.avatar);
        }
    }


    public locationAdapter(List<LocationModel> locationList,Context context) {

        this.locationList = locationList;
        this.context=context;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        @SuppressLint("Recycle") TypedArray circles = context.getResources().obtainTypedArray(R
                .array.circle_images);
        int colorRandom = (int) (Math.random() * circles.length());
        holder.avatar.setText(String.valueOf(position+1));
        holder.avatar.setBackgroundResource((circles.getResourceId(colorRandom, R.drawable
                .circle)));
        LocationModel location = locationList.get(position);
        DecimalToDMS ConverterDMS = new DecimalToDMS();
        String lat = ConverterDMS.decimalToDMS(location.getLatitude());
       String lon = ConverterDMS.decimalToDMS(location.getLongitude());
        String formatLat = lat.replaceAll(context.getString(R.string.characterToReplace), "");
        String formatLon = lon.replaceAll(context.getString(R.string.characterToReplace), "");
        holder.northing.setText(String.format("Lat-%s", formatLat));
        holder.easthing.setText(String.format("Long-%s", formatLon));
        holder.count.setText(String.format("Elev-%s", String.valueOf(location.getElevation())));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }
}