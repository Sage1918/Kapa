package com.example.kapa;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PassengerActivity extends AppCompatActivity {

 ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode() == 102)
                    {
                        Intent intent = result.getData();
                        if(intent == null)
                            Log.e("Error","MAJOR ERROR");
                        else
                        {
                            adapter.lt = intent.getDoubleExtra("latitude",0);
                            adapter.lg = intent.getDoubleExtra("longitude",0);
                        }
                    }

                }
            });
    String user_id;
    String user_name;
    FirebaseDatabase database;
    AdapterPassengerReq adapter;
    List<DriverModel> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("name");

        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("friends").child(user_id);
        Query query = myRef.orderByChild("my_id").equalTo(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot != null){
                    List<String> frndIdList = new ArrayList<>();
                    for(DataSnapshot i : snapshot.getChildren())
                    {
                        String temp = i.getValue(String.class);
                        if(!temp.equals(user_id))
                            frndIdList.add(temp);
                    }

                    fillAdapterList(frndIdList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void fillAdapterList(List<String> frndIdList) {
        DatabaseReference myRef = database.getReference("driver");

            Query query = myRef.orderByChild("user_id");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot != null)
                    {
                        for(DataSnapshot j : snapshot.getChildren())
                        {
                            DriverModel newTemp = j.getValue(DriverModel.class);
                            if(frndIdList.contains(newTemp.getUser_id()))
                            friends.add(newTemp);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }

    // TODO use MyREquest model when pushing to DATABASE
}