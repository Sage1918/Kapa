package com.example.kapa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserStandBy extends AppCompatActivity {

    Button fb_logout_test;
    private String fb_id;
    private String fb_name;
    private String my_Uid;
    boolean isNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stand_by);

        fb_logout_test = findViewById(R.id.LogoutTest);
        fb_logout_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                startActivity(new Intent(UserStandBy.this,MainActivity.class));
                finish();
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        // Accessing the Graph API to get fb_id and fb_name. Here we create a request and later execute it Asynchronously
        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(@Nullable JSONObject jsonObject, @Nullable GraphResponse graphResponse) {
                        try {
                            if (jsonObject != null) {
                                fb_id = jsonObject.getString("id");
                                fb_name = jsonObject.getString("name");
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

        isNewUser = false;
        try {
            // For all databases outside us_central the link needs to be specified.
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference myRef = database.getReference("login");

            Query query = myRef.equalTo(fb_id);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists())
                    {
                        isNewUser = true;
                    }
                    else
                    {
                        // TODO: Get the mode of the User if it is None prompt them to select a mode. If mode is already selected then move to corresponding Activity
                        my_Uid = snapshot.child("Uid").getValue().toString();
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
            Toast.makeText(this, "Cannot Connect to Database", Toast.LENGTH_SHORT).show();
        }

        if(isNewUser) {
            Toast.makeText(this, "Finished part 0/2 database", Toast.LENGTH_SHORT).show();
            addNewUser();
        }
    }

    private void addNewUser() {

        // TODO Get unique key set it as Uid and insert then use this Uid get child and insert LoginType and FriendList
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("login");

        Toast.makeText(this, "Finished part 0/2 database", Toast.LENGTH_SHORT).show();
        String newId = "none";
        while(newId.equals("none")) newId = dbRef.push().getKey();
        LoginTypeModel login = new LoginTypeModel();
        login.setUid(newId);
        login.setLoginId(fb_id);
        login.setLoginType("fb");
        dbRef.child(fb_id).setValue(login);
        Toast.makeText(this, "Finished part 1/2 database", Toast.LENGTH_SHORT).show();

        UserModel newUser = new UserModel();
        newUser.setUname(fb_name);
        newUser.setUid(newId);
        newUser.setMode("None");
        newUser.setScore(0);
        dbRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("user");
        dbRef.child(newId).setValue(newUser);
        Toast.makeText(this, "Finished part 2/2 database", Toast.LENGTH_SHORT).show();

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
                            // TODO change this temp value
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

        */

    }


}