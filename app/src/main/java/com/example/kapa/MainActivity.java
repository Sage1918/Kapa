package com.example.kapa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


public class MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LoginButton fbButton = findViewById(R.id.LoginFacebook);
        Button mailButton = findViewById(R.id.LoginMail);
        Button phoneButton = findViewById(R.id.LoginPhone);
        callbackManager = CallbackManager.Factory.create();


        // com.facebook.AccessToken represents immutable access token and it's metadata like whether it expired or not. See documentation for more info.
        AccessToken accessToken = AccessToken.getCurrentAccessToken();



        // If user still logged in then no need to make them go through the login screen again.
        if(accessToken != null && !accessToken.isExpired())
        {
            startActivity(new Intent(MainActivity.this,UserStandBy.class));
            finish();
        }

        fbButton.setPermissions("user_friends");
        fbButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        // App code
                        // This will be changed to check with the database whether the account is already registered or is it new, Before moving to next activity.
                        // Done: check database and move on with life


                        startActivity(new Intent(MainActivity.this,UserStandBy.class));
                        finish();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Toast.makeText(MainActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull FacebookException exception) {
                        // App code
                        Toast.makeText(MainActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();
                    }
                });


        /* The v-> {} is a lambda function in java... as suggested by Android Studio.
        The setOnClickListener() method takes in an object of class android.view.View
        the View class is the Parent class of all views used in the android app.
        The TextView,Button etc all inherit from the View class.
        */

        /* Toast
            It is a short message/pop up in grey(but it can also be in other colours) giving certain information.
            Usually it doesn't interact with the user but it can be made to do so.
         */

        // TODO implement Login with mail (Optional)
        mailButton.setOnClickListener(v -> Toast.makeText(MainActivity.this,"NOT IMPLEMENTED YET",Toast.LENGTH_SHORT).show());

        // TODO implement Login with phone number
        phoneButton.setOnClickListener(v -> Toast.makeText(MainActivity.this,"NOT IMPLEMENTED YET",Toast.LENGTH_SHORT).show());
    }

      @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}