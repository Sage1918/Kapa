package com.example.kapa;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AdapterAddFriendRequestRv extends RecyclerView.Adapter<AdapterAddFriendRequestRv.MyViewHolder> {

    List<FriendReqModel> myReqlist;
    FirebaseDatabase database;
    String myName;

    AdapterAddFriendRequestRv(List<FriendReqModel> reqlist, FirebaseDatabase db,String myName)
    {
        this.myReqlist = reqlist;
        this.database = db;
        this.myName = myName;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.req_recyclerview_addfriend_layout,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.rv_addFrd_frdName.setText(myReqlist.get(position).getFromUname());
        holder.rv_addFrd_frdUid.setText("UID: " + myReqlist.get(position).getFromUid());

        holder.rv_addFrd_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference myRef = database.getReference("friend");
                myRef.child(myReqlist.get(position).getToUid()).child("my_id").setValue(myReqlist.get(position).getToUid());
                myRef.child(myReqlist.get(position).getToUid()).child(myReqlist.get(position).getFromUid()).setValue(myReqlist.get(position).getFromUid());
                myRef.child(myReqlist.get(position).getFromUid()).child("my_id").setValue(myReqlist.get(position).getFromUid());
                myRef.child(myReqlist.get(position).getFromUid()).child(myReqlist.get(position).getFromUid()).setValue(myReqlist.get(position).getFromUid());
                holder.rv_addFrd_accept.setVisibility(View.INVISIBLE);
                holder.rv_addFrd_reject.setVisibility(View.INVISIBLE);
            }
        });

        holder.rv_addFrd_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference myRef = database.getReference("friend_req")
                        .child(myReqlist.get(position).getToUid()).child(myReqlist.get(position).getFromUid());
                myRef.removeValue();

                holder.rv_addFrd_accept.setVisibility(View.INVISIBLE);
                holder.rv_addFrd_reject.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myReqlist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView rv_addFrd_frdName,rv_addFrd_frdUid;
        Button rv_addFrd_accept,rv_addFrd_reject;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            rv_addFrd_frdName = itemView.findViewById(R.id.rv_addFrd_frdName);
            rv_addFrd_frdUid = itemView.findViewById(R.id.rv_addFrd_frdUid);
            rv_addFrd_accept = itemView.findViewById(R.id.rv_addFrd_accept);
            rv_addFrd_reject = itemView.findViewById(R.id.rv_addFrd_reject);
        }
    }
}
