package com.example.kapa;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.kapa.databinding.ActivityMapsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private String type;
    private LatLng myMarker;
    private FloatingActionButton selectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = getIntent().getStringExtra("type");
        if(!type.equals("select"))
            findViewById(R.id.select_button_in_map).setVisibility(View.INVISIBLE);


        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if(type.equals("select"))
        {
            //Waiting for the map to load
            while(mMap == null);
            mapMakeSelection();
        }
    }

    private void mapMakeSelection() {

        MarkerOptions myMOptions = new MarkerOptions();
        selectBtn = findViewById(R.id.select_button_in_map);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myMarker == null)
                    Toast.makeText(MapsActivity.this, "Please Select a place first", Toast.LENGTH_SHORT).show();
                else
                {
                    // TODO return the x and y coordinates to the other activity
                    Intent intent = new Intent();
                    intent.putExtra("latitude",myMarker.latitude);
                    intent.putExtra("longitude",myMarker.longitude);

                    Intent haha = getIntent();
                    String identifier;
                    identifier = haha.getStringExtra("selecting");
                    if(identifier.equals("from"))
                        setResult(100,intent);
                    else if(identifier.equals("to"))
                        setResult(101,intent);
                    finish();
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                myMarker = latLng;
                myMOptions.position(myMarker).title("From here");
                myMOptions.position(latLng);
                mMap.clear();
                mMap.addMarker(myMOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myMarker));
            }
        });

    }
}