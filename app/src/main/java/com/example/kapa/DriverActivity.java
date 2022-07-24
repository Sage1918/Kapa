package com.example.kapa;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class DriverActivity extends AppCompatActivity {

    private String user_id;
    private String fb_name;
    private FirebaseDatabase database;
    private TextView notTime,ready,driverDetail,from,to,time,noOfSeats,date,reqMsg;
    private Button start,selectFrom,selectTo,done,timeEnter,dateEnter;
    private EditText noOfSeatsEnter;
    //private RecyclerView recyclerView;
    //private RecyclerView.Adapter adapter;
    //private RecyclerView.LayoutManager layoutManager;
    private boolean[] checks;
    private DriverModel driver;

    Calendar cal;

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("AfterWarp","Help.....");

                    if(result.getResultCode() == 100)
                    {
                        Intent intent = result.getData();
                        if(intent == null)
                            Log.d("AfterWarp","Something went WRONG");
                        else
                        {
                            driver.setFrom_latitude(intent.getDoubleExtra("latitude",0));
                            driver.setFrom_longitude(intent.getDoubleExtra("longitude",0));

                            String newTemp = "From:\n" + driver.getFrom_latitude() + ", \n" + driver.getFrom_longitude();
                            from.setText(newTemp);
                            selectFrom.setVisibility(View.INVISIBLE);
                        }
                    }
                    else if(result.getResultCode() == 101)
                    {
                        Intent intent = result.getData();
                        if(intent == null)
                            Log.d("AfterWarp","Something went WRONG");
                        else
                        {
                            driver.setTo_latitude(intent.getDoubleExtra("latitude",0));
                            driver.setTo_longitude(intent.getDoubleExtra("longitude",0));

                            String newTemp = "To:\n" + driver.getTo_latitude() + ", \n" + driver.getTo_longitude();
                            to.setText(newTemp);
                            selectTo.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        user_id = getIntent().getStringExtra("userid");
        fb_name = getIntent().getStringExtra("name");
        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Initialising all text views in the layout
        ready = findViewById(R.id.readyMsg);
        driverDetail = findViewById(R.id.DriverCarpoolDetail);
        from = findViewById(R.id.fromLoc);
        to = findViewById(R.id.toLoc);
        noOfSeats = findViewById(R.id.noOfSeats);
        time = findViewById(R.id.timeOfCarpool);
        date = findViewById(R.id.dateOfCarpool);
        reqMsg = findViewById(R.id.reqMsg);

        // Initialising all buttons in the layout
        start = findViewById(R.id.startButton);
        selectFrom = findViewById(R.id.fromSelectionBtn);
        selectTo = findViewById(R.id.toSelectionBtn);
        done = findViewById(R.id.driverDone);
        timeEnter = findViewById(R.id.timeOfCarpoolEntered);
        dateEnter = findViewById(R.id.dateOfCarpoolEntered);

        // Initialising all editTexts in the layout
        noOfSeatsEnter = findViewById(R.id.noOfSeatsEnteredByDriver);

        // Initialising recycler view
        //recyclerView = findViewById(R.id.rv_req);
        //layoutManager = new LinearLayoutManager(this);
        //adapter

        //recyclerView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setAdapter(adapter);
        // Passing an object to an inner class requires it to be final, so android studio made me do this.
        final String[] dbTime = new String[1];

        checks = new boolean[5];
        for(int i=0;i<3;++i)
            checks[i] = false;

        DatabaseReference myRef = database.getReference("drivers");
        Query query = myRef.orderByChild("user_id");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    driver = new DriverModel();
                    driver.setUser_id(user_id);
                    addDriver();
                }
                else
                {
                    //TODO check time of Car Pool and if system time is close to it then prompt for next activity: The MapActivity
                    Toast.makeText(DriverActivity.this, "The driver is found in Driver node of Database", Toast.LENGTH_SHORT).show();
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


        // Gets the from part of the carpool
        selectFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverActivity.this,MapsActivity.class);
                intent.putExtra("type","select");
                intent.putExtra("selecting","from");
                activityResultLauncher.launch(intent);

                checks[0] = true;
            }

        });

        // Gets the to part of the carpool
        selectTo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverActivity.this,MapsActivity.class);
                intent.putExtra("type","select");
                intent.putExtra("selecting","to");
                activityResultLauncher.launch(intent);

                checks[1] = true;
            }
        });

        // Gets the time of carpool
        timeEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePicker = new TimePickerDialog(
                        DriverActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                int selectTime;
                                selectTime = hourOfDay;
                                selectTime *=100;
                                selectTime += minute;

                                driver.setTime(selectTime);
                                checks[3] = true;

                                // Ternary operator for aesthetic display of values sake...
                                time.setText("Time:\n" + ((selectTime/100 == 0)?"00":selectTime/100) + ":" + ((selectTime % 100 ==0)?"00":selectTime % 100));
                                timeEnter.setVisibility(View.INVISIBLE);
                            }
                        },12,0,false
                );

                timePicker.show();
            }
        });

        // Gets the date of carpool
        dateEnter.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                // Initial value of the datePicker will be today's date
                cal = Calendar.getInstance();
                int yearNow = cal.get(Calendar.YEAR);
                int monthNow = cal.get(Calendar.MONTH);
                int dayNow = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePicker = new DatePickerDialog(
                        DriverActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Checks if date is valid
                                if(year < yearNow)
                                    Toast.makeText(DriverActivity.this, "Too Late for that, Fancy Time Travelling??", Toast.LENGTH_SHORT).show();
                                else if(year == yearNow && month < monthNow)
                                    Toast.makeText(DriverActivity.this, "Too Late for that, Fancy Time Travelling??", Toast.LENGTH_SHORT).show();
                                else if(year == yearNow && month == monthNow && dayOfMonth < dayNow)
                                    Toast.makeText(DriverActivity.this, "Too Late for that, Fancy Time Travelling??", Toast.LENGTH_SHORT).show();
                                else if(year == yearNow && month == monthNow && dayOfMonth == dayNow)
                                {
                                    int currentTime = cal.get(Calendar.HOUR_OF_DAY) * 100 + cal.get(Calendar.MINUTE);
                                    if(driver.getTime() < currentTime)
                                        Toast.makeText(DriverActivity.this, "Too Late for that, Fancy Time Travelling??", Toast.LENGTH_SHORT).show();

                                }
                                else
                                {
                                    //Date is valid
                                    int selectDate = year;
                                    selectDate *= 100;
                                    selectDate += month+1;
                                    selectDate *= 100;
                                    selectDate += dayOfMonth;

                                    driver.setDate(selectDate);
                                    checks[4] = true;

                                    // month parameter starts from 0
                                    date.setText("Date:\n"+dayOfMonth+" "+getMonth(month+1)+" "+year );
                                    dateEnter.setVisibility(View.INVISIBLE);
                                }
                            }

                            // a function to get the name of month from its numerical value
                            private String getMonth(int i) {
                                switch (i)
                                {
                                    case 1: return "Jan";
                                    case 2: return "Feb";
                                    case 3: return "Mar";
                                    case 4: return "Apr";
                                    case 5: return "May";
                                    case 6: return "Jun";
                                    case 7: return "Jul";
                                    case 8: return "Aug";
                                    case 9: return "Sep";
                                    case 10: return "Oct";
                                    case 11: return "Nov";
                                    case 12: return "Dec";

                                    default: return "Err";
                                }
                            }
                        },
                        yearNow,
                        monthNow,
                        dayNow
                );

                datePicker.show();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nos = 0;
                String temp = noOfSeatsEnter.getText().toString();
                if(!temp.equals("")) {
                    nos = Integer.parseInt(temp);
                }

                // Checks that number of seats are reasonable and not absurd
                if(nos > 9)
                    Toast.makeText(DriverActivity.this, "Are you sure you're not driving a bus...??", Toast.LENGTH_SHORT).show();
                else if(nos != 0)
                {
                    driver.setNumberOfSeats(nos);
                    checks[2] = true;
                }

                // checks if all values are entered
                boolean allClear = true;
                for(boolean i :checks)
                {
                    if(!i)
                        allClear = false;
                }

                if(allClear)
                {
                    Toast.makeText(DriverActivity.this, "You Entered Everything...", Toast.LENGTH_SHORT).show();
                    database.getReference("user").child(user_id).child("mode").setValue("Driver");
                    myRef.child(user_id).setValue(driver);

                    // TODO determine where to go from here...


                }
                else
                    Toast.makeText(DriverActivity.this, "Please fill all details...", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showAddDriverView()
    {

        // Changes the layout to show all Views requires to enter the information for adding a new driver and makes others invisible
        ready.setVisibility(View.INVISIBLE);
        start.setVisibility(View.INVISIBLE);
        reqMsg.setVisibility(View.INVISIBLE);

        driverDetail.setVisibility(View.VISIBLE);
        from.setVisibility(View.VISIBLE);
        to.setVisibility(View.VISIBLE);
        noOfSeats.setVisibility(View.VISIBLE);
        time.setVisibility(View.VISIBLE);
        date.setVisibility(View.VISIBLE);

        selectTo.setVisibility(View.VISIBLE);
        selectFrom.setVisibility(View.VISIBLE);
        done.setVisibility(View.VISIBLE);

        noOfSeatsEnter.setVisibility(View.VISIBLE);
        timeEnter.setVisibility(View.VISIBLE);
        dateEnter.setVisibility(View.VISIBLE);
    }

    void showReadyView()
    {
        // TODO changes the layout to show all Views that are need to be shown when the present time equals the time of carpool
    }

    void showRequestView()
    {
        // TODO changes the layout to show all Views that are need to be shown to facilitate accepting and rejecting of ride requests from the passengers
        // This will be displayed by the Recycler View

    }
}