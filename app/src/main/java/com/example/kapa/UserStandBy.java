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

import org.json.JSONException;
import org.json.JSONObject;

public class UserStandBy extends AppCompatActivity {

    Button fb_logout_test;
    private DatabaseReference myRef;
    private String fb_id;
    private String fb_name;

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

        try {
            // For all databases outside us_central the link needs to be specified.
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app/");
            myRef = database.getReference("user");

            Query query = myRef.child("user").orderByChild("id").equalTo(fb_id);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists())
                    {
                        // TODO insert user into database
                        UserModel newuser = new UserModel();
                        newuser.setUname(fb_name);
                        newuser.setLt(new LoginTypeModel());
                        newuser.getLt().setFb_id(fb_id);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Cannot Connect to Database", Toast.LENGTH_SHORT).show();
        }
    }

}