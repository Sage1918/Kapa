package com.example.kapa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;

public class UserStandBy extends AppCompatActivity {

    Button fb_logout_test;

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
    }

}