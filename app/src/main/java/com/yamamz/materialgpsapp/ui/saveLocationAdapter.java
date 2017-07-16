package com.yamamz.materialgpsapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yamamz.materialgpsapp.R;
import com.yamamz.materialgpsapp.locDetails;
import com.yamamz.materialgpsapp.model.SaveLocation;

import java.util.List;

/**
 * Created by AMRI on 1/22/2017.
 */

public class saveLocationAdapter extends RecyclerView.Adapter<saveLocationAdapter.myViewHolder> {

    private Context context;
    private List<SaveLocation> saveLocationList;

    public class myViewHolder extends RecyclerView.ViewHolder {
        private TextView filename,initial;
        final View mView;


        public myViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            filename=(TextView) itemView.findViewById(R.id.fileName);
            initial=(TextView) itemView.findViewById(R.id.avatar);

        }
    }

    public saveLocationAdapter(Context context,List<SaveLocation> saveLocationList) {
        this.saveLocationList = saveLocationList;
        this.context = context;
    }





    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.save_location_list, parent, false);
                return new myViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(myViewHolder holder, int position) {
        final SaveLocation saveLocation=saveLocationList.get(position);

        @SuppressLint("Recycle") TypedArray circles = context.getResources().obtainTypedArray(R
                .array.circle_images);
        int colorRandom = (int) (Math.random() * circles.length());
        holder.filename.setText(saveLocation.getFileName());

        String name= holder.filename.getText().toString();
     if(name.length()>0) {
         holder.initial.setText(name.substring(0, 1));
         holder.initial.setBackgroundResource((circles.getResourceId(colorRandom, R.drawable
                 .circle)));
     }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, locDetails.class);
                intent.putExtra("fileName",saveLocation.getFileName());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return saveLocationList.size();
    }


}
