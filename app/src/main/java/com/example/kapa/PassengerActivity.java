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
import java.util.Calendar;
import java.util.List;

public class PassengerActivity extends AppCompatActivity {

 ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode() == 102)
                    {
                        isPermanent = true;
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
    private String drid;
    private FirebaseDatabase database;
    private RecyclerView recyclerView_passenger;
    private AdapterPassengerReq adapter;
    private List<DriverModel> friends;
    private Button toggle;
    private boolean isPermanent;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        isPermanent = false;

        Intent intent = getIntent();
        user_id = intent.getStringExtra("userid");
        user_name = intent.getStringExtra("name");

        DatabaseReference newMyRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user").child(user_id);
        newMyRef.child("mode").setValue("Passenger");


        recyclerView_passenger = findViewById(R.id.rv_PassengerReq);
        recyclerView_passenger.setLayoutManager(new LinearLayoutManager(this));
        recyclerView_passenger.setHasFixedSize(true);
        friends = new ArrayList<>();
        toggle = findViewById(R.id.toggle);

        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");

        DatabaseReference myRef = database.getReference("carpool");
        Query query = myRef.orderByChild(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean some = false;
                if(snapshot.exists())
                for(DataSnapshot i : snapshot.getChildren())
                {
                    if(i.child("People").child(user_id).exists())
                    {
                        drid = i.child("People").child("drid").getValue(String.class);
                        checkTime(drid);
                        some = true;
                    }
                }

                if(some)
                {
                    fillFriendList();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkTime(String d) {
        DatabaseReference myRef = database.getReference("driver");
        Query query = myRef.orderByChild("user_id").equalTo(d);
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            DriverModel driver = null;
                            for (DataSnapshot i : snapshot.getChildren()) {
                                driver = i.getValue(DriverModel.class);
                            }

                            Calendar cal = Calendar.getInstance();
                            int myDate = cal.get(cal.YEAR)*10000 + (cal.get(cal.MONTH)+1)*100 + cal.get(cal.DAY_OF_MONTH);
                            Log.d("TodayDate",String.valueOf(myDate));

                           if (driver.getDate() == myDate) {

                                int myTime;

                                myTime = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
                                if (driver.getTime() - myTime <=200) {
                                    // TODO start MapActivity accordingly...
                                    Toast.makeText(PassengerActivity.this, "Time to start Carpool...", Toast.LENGTH_SHORT).show();
                                    DriverModel finalDriver = driver;
                                    toggle.setVisibility(View.VISIBLE);
                                    recyclerView_passenger.setVisibility(View.INVISIBLE);
                                    toggle.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(PassengerActivity.this,MapsActivity.class);
                                            intent.putExtra("type","ride");
                                            intent.putExtra("mode","passenger");
                                            intent.putExtra("user_id",user_id);
                                            intent.putExtra("drid",finalDriver.getUser_id());
                                            intent.putExtra("tolat", finalDriver.getTo_latitude());
                                            intent.putExtra("tolog", finalDriver.getTo_longitude());
                                            startActivity(intent);
                                            finish();
                                        }
                                    });

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("DatabaseERROR",error.getMessage());
                    }
                }
        );

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

                        adapter = new AdapterPassengerReq(friends,PassengerActivity.this,activityResultLauncher,user_name,user_id);
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

    private void fillFriendList()
    {
        DatabaseReference myRef = database.getReference("friend").child(user_id);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
    @Override
    public void onBackPressed()
    {
        // If user have confirmed in becoming driver of Carpool then back button will not goto mode selection
        if(isPermanent)
        {

            if(backPressedTime + 2000 > System.currentTimeMillis())
            {
                super.onBackPressed();
                return;
            }
            else
                Toast.makeText(PassengerActivity.this,"Press Back again to exit",Toast.LENGTH_SHORT).show();

            backPressedTime = System.currentTimeMillis();
        }
        // If user is yet to have confirmed then they may go back and change to passenger or something
        else
        {
            // rerolling the changes to database entries and restarting the mode selection activity with the required parameters.
            database.getReference("user").child(user_id).child("mode").setValue("None");
            DatabaseReference myRef = database.getReference("driver");
            myRef.child(user_id).removeValue();
            Intent intent = new Intent(PassengerActivity.this,UserStandBy.class);
            intent.putExtra("user_id",user_id);
            intent.putExtra("name",user_name);
            startActivity(intent);
            finish();
        }
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


}