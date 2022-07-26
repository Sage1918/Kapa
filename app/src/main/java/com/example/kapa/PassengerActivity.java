package com.example.kapa;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.login.LoginManager;
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
    private String user_id;
    private String user_name;
    private FirebaseDatabase database;
    private RecyclerView recyclerView_passenger;
    private RecyclerView recyclerView_pendingReq;
    private AdapterPassengerReq adapter;
    private List<DriverModel> friends;
    private Button toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("name");
        recyclerView_passenger = findViewById(R.id.rv_PassengerReq);
        recyclerView_passenger.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_passenger.setHasFixedSize(true);
        recyclerView_pendingReq = findViewById(R.id.pending_requests);
        recyclerView_pendingReq.setLayoutManager(new LinearLayoutManager(this));

        toggle = findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recyclerView_passenger.getVisibility() == View.VISIBLE)
                    recyclerView_passenger.setVisibility(View.INVISIBLE);
                else
                    recyclerView_passenger.setVisibility(View.VISIBLE);

                if(recyclerView_pendingReq.getVisibility() == View.VISIBLE)
                    recyclerView_pendingReq.setVisibility(View.INVISIBLE);
                else
                    recyclerView_pendingReq.setVisibility(View.VISIBLE);

            }
        });

        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference myRef = database.getReference("friend");
        Query query = myRef.orderByChild("my_id").equalTo(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    List<String> frndIdList = new ArrayList<>();
                    for(DataSnapshot i : snapshot.getChildren())
                    {
                        String temp = i.getValue(String.class);
                        if(!temp.equals(user_id))
                            frndIdList.add(temp);
                    }

                    fillAdapterList(frndIdList);
                }
                else
                    Toast.makeText(PassengerActivity.this, "You Currently do not have any friends. Add some using the option menu above.", Toast.LENGTH_SHORT).show();
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
                    if(snapshot.exists())
                    {
                        for(DataSnapshot j : snapshot.getChildren())
                        {
                            DriverModel newTemp = j.getValue(DriverModel.class);
                            if(frndIdList.contains(newTemp.getUser_id()))
                            friends.add(newTemp);
                        }

                        adapter = new AdapterPassengerReq(friends,PassengerActivity.this,activityResultLauncher);
                        recyclerView_passenger.setAdapter(adapter);

                    }
                    else{
                        Toast.makeText(PassengerActivity.this, "You don't have any friends. Add friends from the Settings menu", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);

        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.mn_Logout:
                LoginManager.getInstance().logOut();
                startActivity(new Intent(PassengerActivity.this,MainActivity.class));
                finish();
                break;
            case R.id.mn_addFriends:
                // TODO add friends
                Intent intent = new Intent(PassengerActivity.this,AddFriends.class);
                intent.putExtra("user_id",user_id);
                intent.putExtra("user_name",user_name);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO use MyREquest model when pushing to DATABASE
}