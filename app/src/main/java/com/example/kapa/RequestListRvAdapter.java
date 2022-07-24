package com.example.kapa;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RequestListRvAdapter extends RecyclerView.Adapter<RequestListRvAdapter.MyViewHolder> {

    List<MyRequest> myRequestList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameOfReqestee;
        TextView idOfRequestee;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            //nameOfReqestee = itemView.findViewById();
        }
    }
}