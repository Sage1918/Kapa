package com.example.kapa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterPassengerReq extends RecyclerView.Adapter<AdapterPassengerReq.MyViewHolder> {

    List<DriverModel> myFriends;
    Context context;
    ActivityResultLauncher<Intent> activityResultLauncher;
    double lt,lg;


    public AdapterPassengerReq(List<DriverModel> myFriends, Context context,ActivityResultLauncher<Intent> a) {
        this.myFriends = myFriends;
        this.context = context;
        this.activityResultLauncher = a;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.req_recyclerview_passenger_layout,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.rv_pass_name.setText(myFriends.get(position).getUser_name());
        holder.rv_pass_from.setText("From:\nLatitude: " + String.valueOf(myFriends.get(position).getFrom_latitude()
                                    + "\nLongitude: " + String.valueOf(myFriends.get(position).getFrom_longitude())));
        holder.rv_pass_to.setText("To:\nLatitude: " + String.valueOf(myFriends.get(position).getTo_latitude()
                + "\nLongitude: " + String.valueOf(myFriends.get(position).getTo_longitude())));
        holder.rv_pass_nos.setText(String.valueOf(myFriends.get(position).getNumberOfSeats()));


        holder.rv_pass_seeOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double flt,flg,tlt,tlg;
                flt = myFriends.get(position).getFrom_latitude();
                flg = myFriends.get(position).getFrom_longitude();
                tlt = myFriends.get(position).getTo_latitude();
                tlg = myFriends.get(position).getTo_longitude();

                Intent i = new Intent(context,MapsActivity.class);

                i.putExtra("type","seeOnMap");
                i.putExtra("subtype","Passenger");
                i.putExtra("flat",flt);
                i.putExtra("flog",flg);
                i.putExtra("lat",tlt);
                i.putExtra("log",tlg);

                // TODO goto Map Activity and show the location
                context.startActivity(i);
            }
        });

        holder.rv_pass_pickupLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,MapsActivity.class);
                i.putExtra("type","select");
                i.putExtra("selecting","pickup");
                activityResultLauncher.launch(i);

                holder.rv_pass_pickLocDisp.setText("Location Chosen\nlat: " + lt + "\nlog: " + lg);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myFriends.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView rv_pass_name;
        TextView rv_pass_from;
        TextView rv_pass_to;
        TextView rv_pass_nos;
        TextView rv_pass_time;
        TextView rv_pass_date;
        TextView rv_pass_pickLocDisp;

        Button rv_pass_seeOnMap;
        Button rv_pass_pickupLoc;
        Button rv_pass_request;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            rv_pass_name = itemView.findViewById(R.id.nameODriver);
            rv_pass_from = itemView.findViewById(R.id.rv_driverFrom);
            rv_pass_to = itemView.findViewById(R.id.rv_driverTo);
            rv_pass_nos = itemView.findViewById(R.id.rv_numberOfSeatsRemain);
            rv_pass_time = itemView.findViewById(R.id.rv_pass_time);
            rv_pass_date = itemView.findViewById(R.id.rv_pass_date);
            rv_pass_pickLocDisp = itemView.findViewById(R.id.rv_pickLocDisp);
            rv_pass_seeOnMap = itemView.findViewById(R.id.rv_driver_seeOnMap);
            rv_pass_pickupLoc = itemView.findViewById(R.id.rv_pickPassengerPickupLocation);
            rv_pass_request = itemView.findViewById(R.id.rv_reqDriverByPassenger);
        }
    }
}
