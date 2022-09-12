package com.example.kapa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterFriendListRv extends RecyclerView.Adapter<AdapterFriendListRv.MyViewHolder> {
    List<FriendModel> friends;

    AdapterFriendListRv(List<FriendModel> friends)
    {
        this.friends = friends;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.req_recyclerview_friendslist_layout,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.rv_mn_usr_id.setText("User ID: " + friends.get(position).getUser_id());
        holder.rv_mn_name.setText("Name: " + friends.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView rv_mn_usr_id,rv_mn_name;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            rv_mn_usr_id = itemView.findViewById(R.id.rv_FriendsList_Uid);
            rv_mn_name = itemView.findViewById(R.id.rv_FriendsList_Name);
        }
    }
}
