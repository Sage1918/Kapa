package com.example.kapa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterRequestListRv extends RecyclerView.Adapter<AdapterRequestListRv.MyViewHolder> {

    // The adapter for the recycle view that displays the list of passengers that request a driver for
    // carpool. it also includes buttons to see the location on map, accept and reject.
    List<MyRequestModel> myRequestModelList;
    Context context;

    public AdapterRequestListRv(List<MyRequestModel> myRequestModelList, Context context) {
        this.myRequestModelList = myRequestModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.req_recyclerview_layout,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    //Dirty way of solving the "position variable is not fixed" concern of Android Studio
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.nameOfRequestee.setText(myRequestModelList.get(position).name);
        holder.lati.setText("latitude: " + String.valueOf(myRequestModelList.get(position).getPickupLat()));
        holder.longi.setText("longitude: " + String.valueOf(myRequestModelList.get(position).getPickupLong()));

        holder.rv_seeOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lt,lg;
                lt = myRequestModelList.get(position).getPickupLat();
                lg = myRequestModelList.get(position).getPickupLong();

                Intent i = new Intent(context,MapsActivity.class);

                i.putExtra("type","seeOnMap");
                i.putExtra("lat",lt);
                i.putExtra("log",lg);

                context.startActivity(i);
            }
        });

        holder.rv_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO accept the passenger and add to final carpool node. May disable both accept and reject button for this entry now...

                holder.rv_reject.setVisibility(View.INVISIBLE);
                holder.rv_accept.setVisibility(View.INVISIBLE);
            }
        });

        holder.rv_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO reject the passenger and convey the meaning to the passenger side

                holder.rv_reject.setVisibility(View.INVISIBLE);
                holder.rv_accept.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myRequestModelList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameOfRequestee;
        TextView lati;
        TextView longi;
        Button rv_accept,rv_reject,rv_seeOnMap;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nameOfRequestee = itemView.findViewById(R.id.rv_nameopass);
            lati = itemView.findViewById(R.id.rv_latitude);
            longi = itemView.findViewById(R.id.rv_lognitude);
            rv_accept = itemView.findViewById(R.id.rv_accept);
            rv_reject = itemView.findViewById(R.id.rv_reject);
            rv_seeOnMap = itemView.findViewById(R.id.rv_see_on_map);
        }
    }
}