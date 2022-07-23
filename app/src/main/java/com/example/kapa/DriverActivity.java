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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DriverActivity extends AppCompatActivity {

    private String user_id;
    private String fb_name;
    private FirebaseDatabase database;
    private TextView notTime,ready,driverDetail,from,to,time,noOfSeats;
    private Button start,selectFrom,selectTo,done;
    private EditText noOfSeatsEnter,timeEnter;
    private boolean checks[];

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("BeforeWarp","Help.....");
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        user_id = getIntent().getStringExtra("userid");
        fb_name = getIntent().getStringExtra("name");
        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Initialising all views in the layout
        notTime = findViewById(R.id.waitMsg);
        ready = findViewById(R.id.readyMsg);
        driverDetail = findViewById(R.id.DriverCarpoolDetail);
        from = findViewById(R.id.fromLoc);
        to = findViewById(R.id.toLoc);
        time = findViewById(R.id.timeOfCarpool);
        noOfSeats = findViewById(R.id.noOfSeats);

        // Initialising all buttons in the layout
        start = findViewById(R.id.startButton);
        selectFrom = findViewById(R.id.fromSelectionBtn);
        selectTo = findViewById(R.id.toSelectionBtn);
        done = findViewById(R.id.driverDone);

        // Initialising all edittexts in the layout
        noOfSeatsEnter = findViewById(R.id.noOfSeatsEnteredByDriver);
        timeEnter = findViewById(R.id.timeOfCarpoolEntered);

        // Passing an object to an inner class requires it to be final, so android studio made me do this.
        final String[] dbTime = new String[1];

        checks = new boolean[4];
        for(int i=0;i<3;++i)
            checks[i] = false;

        DatabaseReference myRef = database.getReference("drivers");
        Query query = myRef.orderByChild("userid");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                    addDriver();
                else
                {
                    //TODO check time of Car Pool and if system time is close to it then prompt for next activity: The MapActivity
                    for(DataSnapshot i : snapshot.getChildren())
                    {
                        if(i.getKey().equals("time"))
                            dbTime[0] = i.getValue().toString();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Database",error.getMessage());
            }
        });
    }

    private void addDriver() {
        //TODO add the driver to database
        showAddDriverView();
        DatabaseReference myRef = database.getReference("driver");



        selectFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverActivity.this,MapsActivity.class);
                intent.putExtra("type","select");
                activityResultLauncher.launch(intent);
            }

        });

        selectTo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverActivity.this,MapsActivity.class);
                intent.putExtra("type","select");
                activityResultLauncher.launch(intent);
            }
        });
    }

    private void showAddDriverView()
    {
        notTime.setVisibility(View.INVISIBLE);
        ready.setVisibility(View.INVISIBLE);
        start.setVisibility(View.INVISIBLE);

        driverDetail.setVisibility(View.VISIBLE);
        from.setVisibility(View.VISIBLE);
        to.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);
        noOfSeats.setVisibility(View.VISIBLE);

        selectTo.setVisibility(View.VISIBLE);
        selectFrom.setVisibility(View.VISIBLE);
        done.setVisibility(View.VISIBLE);

        noOfSeatsEnter.setVisibility(View.VISIBLE);
        timeEnter.setVisibility(View.VISIBLE);
    }
}