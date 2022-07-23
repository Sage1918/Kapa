package com.example.kapa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DriverActivity extends AppCompatActivity {

    String user_id;
    String fb_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        user_id = savedInstanceState.getString("userid");
        fb_name = savedInstanceState.getString("name");
    }
}