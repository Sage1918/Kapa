package com.example.kapa;

import static android.util.Log.d;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;


public class UserStandBy extends AppCompatActivity {

    private String fb_id;
    private String fb_name;
    private String my_Uid;
    boolean isNewUser;
    private String userMode;
    private FirebaseDatabase database;
    TextView greet,beDriver,bePassenger;
    Button driver,passenger;
    Menu menu;
    UserModel newUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stand_by);

        driver = findViewById(R.id.DriverButton);
        passenger = findViewById(R.id.PassengerButton);
        beDriver = findViewById(R.id.BeDriver);
        bePassenger = findViewById(R.id.BePassenger);
        greet = findViewById(R.id.greet);

        //
        hideViews();

        // If we came back from Driver before Confirming Driver...
        Intent intent = getIntent();
        if(intent.getStringExtra("back")!=null)
        {
            my_Uid = intent.getStringExtra("user_id");
            fb_name = intent.getStringExtra("name");

            getDetails(my_Uid,false);
        }

        // Accessing the Graph API to get fb_id and fb_name. Here we create a request and later execute it Asynchronously

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                        try {
                            if (jsonObject != null) {
                                fb_id = jsonObject.getString("id");
                                fb_name = jsonObject.getString("name");

                                greet.setText("Hello "+fb_name);



                                try {
                                    // For all databases outside us_central the link needs to be specified.
                                    database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");
                                    DatabaseReference myRef = database.getReference("login");

                                    Query query = myRef.orderByChild("loginId").equalTo(fb_id);
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Log.d("SNAPSHOT exist or Nah",(snapshot.exists())?"True":"False");
                                            if(!snapshot.exists())
                                            {
                                                isNewUser = true;
                                            }
                                            else
                                            {
                                                // Done: Get the mode of the User if it is None prompt them to select a mode. If mode is already selected then move to corresponding Activity
                                                for( DataSnapshot ss : snapshot.getChildren())
                                                {
                                                    my_Uid = ss.child("userid").getValue().toString();
                                                    isNewUser = false;
                                                }

                                            }

                                            if(isNewUser)
                                            {
                                                Log.d("Database Search RESULT","SEARCHED and NOT FOUND");
                                                Log.d("Value of FBID",fb_id);
                                                addNewUser();
                                            }
                                            else
                                            {
                                                Log.d("Database Search RESULT", "SEARCHED and FOUND");

                                                getDetails(my_Uid,false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            System.out.println(error.getMessage());
                                        }
                                    });
                                }
                                catch (Exception e)
                                {
                                    Toast.makeText(UserStandBy.this, "Cannot Connect to Database", Toast.LENGTH_SHORT).show();
                                }




                                Log.d("isNewUser",(isNewUser)?"Ture":"Flase");


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields","id,name");
        request.setParameters(parameters);
        request.executeAsync();



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
                startActivity(new Intent(UserStandBy.this,MainActivity.class));
                finish();
                break;
            case R.id.mn_addFriends:
                Intent intent = new Intent(UserStandBy.this,AddFriends.class);
                intent.putExtra("user_id",my_Uid);
                intent.putExtra("user_name",fb_name);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getDetails(String id,boolean justAdded) {

        if(justAdded)
        {
            userMode = newUser.getMode();
            if (userMode.equals("None")) {

                driver.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(UserStandBy.this, DriverActivity.class);
                        i.putExtra("userid", id);
                        i.putExtra("name", fb_name);

                        startActivity(i);
                        finish();
                    }
                });

                passenger.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(UserStandBy.this, PassengerActivity.class);
                        i.putExtra("userid", id);
                        i.putExtra("name", fb_name);

                        startActivity(i);
                        finish();
                    }
                });
            }
            else if (userMode.equals("Driver")) {
                Intent i = new Intent(UserStandBy.this, DriverActivity.class);
                i.putExtra("userid", id);
                i.putExtra("name", fb_name);

                startActivity(i);
                finish();
            }
            else if (userMode.equals("Passenger"))
            {
                Intent i = new Intent(UserStandBy.this, PassengerActivity.class);
                i.putExtra("userid", id);
                i.putExtra("name", fb_name);

                startActivity(i);
                finish();
            }
        }
        else
        {
            DatabaseReference myRef = database.getReference("user").child(id);
            Query query = myRef.orderByChild("mode");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot i : snapshot.getChildren())
                    if(i.getKey().equals("mode"))
                    userMode = i.getValue().toString();

                if (userMode.equals("None")) {
                    unHideViews();

                    driver.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(UserStandBy.this, DriverActivity.class);
                            i.putExtra("userid", id);
                            i.putExtra("name", fb_name);

                                startActivity(i);
                                finish();
                            }
                        });

                        passenger.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(UserStandBy.this, PassengerActivity.class);
                                i.putExtra("userid", id);
                                i.putExtra("name", fb_name);

                                startActivity(i);
                                finish();
                            }
                        });
                    }
                    else if (userMode.equals("Driver")) {
                        Intent i = new Intent(UserStandBy.this, DriverActivity.class);
                        i.putExtra("userid", id);
                        i.putExtra("name", fb_name);

                        startActivity(i);
                        finish();
                    }
                    else if (userMode.equals("Passenger"))
                    {
                        Intent i = new Intent(UserStandBy.this, PassengerActivity.class);
                        i.putExtra("userid", id);
                        i.putExtra("name", fb_name);

                        startActivity(i);
                        finish();
                    }
                    else
                    Log.e("MAJOR ERROR","Something wrong with user mode from Database");

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Database",error.getMessage());
                }
            });
        }
    }
/*
    //private void searchDb(String f_id)
    {
        // Checking the fb_id in database to see if user already registered in Kapa


    }

 */

    private void addNewUser() {
        unHideViews();

        // Added the login Details of new users to login node
        d("ACCESS VALUE(ADD USER)",fb_id);
        DatabaseReference dbRef = database.getReference("login");

        String newId = "none";
        while(newId.equals("none")) newId = dbRef.push().getKey().toString();
        LoginTypeModel login = new LoginTypeModel();
        login.setUserid(newId);
        login.setLoginId(fb_id);
        login.setLoginType("fb");
        dbRef.child(fb_id).setValue(login);
        //Toast.makeText(this, "Finished part 1/3 database", Toast.LENGTH_SHORT).show();

        // Added New user to user node of database

        newUser = new UserModel();
        newUser.setUname(fb_name);
        newUser.setUserStrId(newId);
        newUser.setMode("None");
        newUser.setScore(0);
        dbRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user");
        dbRef.child(newId).setValue(newUser);
        //Toast.makeText(this, "Finished part 2/3 database", Toast.LENGTH_SHORT).show();

        getDetails(fb_id,true);

    /*
        GraphRequest friendsReq = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(@Nullable JSONArray jsonArray, @Nullable GraphResponse graphResponse) {
                ArrayList<String> friendsId = new ArrayList<String>();
                try {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        friendsId.add(jsonArray.getJSONObject(i).getString("id"));

                    }


                } catch (JSONException e) {
                    Log.e("JSON error", e.getMessage());
                }
            }
            });

            */
    }

    private void hideViews()
    {
        beDriver.setVisibility(View.INVISIBLE);
        bePassenger.setVisibility(View.INVISIBLE);
         driver.setVisibility(View.INVISIBLE);
         passenger.setVisibility(View.INVISIBLE);
    }

    private void unHideViews()
    {
        beDriver.setVisibility(View.VISIBLE);
        bePassenger.setVisibility(View.VISIBLE);
         driver.setVisibility(View.VISIBLE);
         passenger.setVisibility(View.VISIBLE);
    }
/*
    private void getFriends()
    {
        //ArrayList<String> friends;
             /*
        GraphRequest reqUserFriends = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(@Nullable JSONArray jsonArray, @Nullable GraphResponse graphResponse) {
                if (jsonArray != null) {
                    DatabaseReference dbRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("user");
                    for(int i=0;i < jsonArray.length();++i)
                    {
                        try {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String Uid = "temp";
                            FriendsModel newFriend = new FriendsModel(
                                    object.getString("name"),
                                    object.getString("id"),
                                    Uid);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }
            }
        });

        reqUserFriends.executeAsync();

    }
        */
}
