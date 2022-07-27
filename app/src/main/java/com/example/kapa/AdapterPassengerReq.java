package com.example.kapa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AdapterPassengerReq extends RecyclerView.Adapter<AdapterPassengerReq.MyViewHolder> {

    List<DriverModel> myFriends;
    Context context;
    ActivityResultLauncher<Intent> activityResultLauncher;
    double lt,lg;
    String myName;
    String myId;
    boolean flag;


    public AdapterPassengerReq(List<DriverModel> myFriends, Context context,ActivityResultLauncher<Intent> a, String myname,String myId) {
        this.myFriends = myFriends;
        this.context = context;
        this.activityResultLauncher = a;
        this.myName = myname;
        this.myId = myId;
        this.flag = false;
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
        holder.rv_pass_nos.setText("Number of Seats: " + String.valueOf(myFriends.get(position).getNumberOfSeats()));
        holder.rv_pass_time.setText("time: " + String.valueOf(myFriends.get(position).getTime()/100) + ":" + ((myFriends.get(position).getTime()%100 < 10)? "0"+String.valueOf(myFriends.get(position).getTime()%100):String.valueOf(myFriends.get(position).getTime()%100)));
        holder.rv_pass_date.setText("date: "+String.valueOf(myFriends.get(position).getDate()%100)+getMonth((myFriends.get(position).getDate()%10000)/100) +String.valueOf(myFriends.get(position).getDate()/10000));



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

                holder.rv_pass_pickLocDisp.setVisibility(View.INVISIBLE);
                holder.rv_pass_pickLocDisp.setText("Location Chosen\nlat: " + lt + "\nlog: " + lg);
                flag = true;
            }
        });

        holder.rv_pass_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference newMyRef =  FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("carpool");
                newMyRef.child(myId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            Toast.makeText(context, "You are already in a Carpool", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            if(flag)
                            {
                                DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("request");
                                MyRequestModel newModel = new MyRequestModel(myFriends.get(position).getUser_id(),myId,myName,lt,lg);
                                myRef.child(myFriends.get(position).getUser_id()).child(myId).setValue(newModel);

                                Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show();
                                holder.rv_pass_pickupLoc.setVisibility(View.INVISIBLE);
                                holder.rv_pass_request.setVisibility(View.INVISIBLE);
                            }
                            else
                                Toast.makeText(context, "Enter the place you prefer to get picked up from", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
    }

    private String getMonth(int i) {
        switch (i)
        {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Apr";
            case 4:
                return "Mar";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
        }
        return  "Non";
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

    public void updateView()
    {

    }
}
