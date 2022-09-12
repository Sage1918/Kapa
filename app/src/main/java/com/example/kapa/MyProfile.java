package com.example.kapa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyProfile extends AppCompatActivity {

    String user_id;
    String name;
    List<FriendModel> MyFriends;
    FirebaseDatabase database;
    RecyclerView myRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        TextView tId = findViewById(R.id.mn_myprofile_uid);
        TextView tName = findViewById(R.id.mn_myprofile_name);
        myRv = findViewById(R.id.mn_myprofile_rv);
        myRv.setLayoutManager(new LinearLayoutManager(this));
        myRv.setHasFixedSize(true);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        name = intent.getStringExtra("user_name");
        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");
        MyFriends = new ArrayList<>();

        tId.setText("User ID: " + user_id);
        tName.setText("Name: " + name);

        DatabaseReference myRef = database.getReference("friend").child(user_id);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getName(MyFriends,snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getName(List<FriendModel> myFriends, DataSnapshot iterateMePls) {
        List<String> FriendIdList = new ArrayList<>();
        DatabaseReference myRef;
        for(DataSnapshot i : iterateMePls.getChildren()) {
            if (!i.getKey().equals("my_id")) {
                FriendIdList.add(i.getValue(String.class));
            }
        }

        for(String i : FriendIdList) {
            myRef = database.getReference("user").child(i).child("uname");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null) {
                        myFriends.add(new FriendModel(i, snapshot.getValue(String.class)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        // Wait till the Async call to Database is done...
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::AsyncIsPain,2000);

    }

    void AsyncIsPain()
    {
        RecyclerView.Adapter adapter = new AdapterFriendListRv(MyFriends);
        myRv.setAdapter(adapter);
        myRv.setVisibility(View.VISIBLE);
    }
}