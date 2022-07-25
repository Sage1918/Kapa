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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
        //if(!type.equals("select"))
          //  findViewById(R.id.select_button_in_map).setVisibility(View.INVISIBLE);


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
        LatLng kerala = new LatLng(10, 76);
        mMap.addMarker(new MarkerOptions().position(kerala).title("A default Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kerala,10f));
        mMap.clear();
        if(type.equals("select"))
        {
            mapMakeSelection();
        }
        else if(type.equals("seeOnMap"))
        {
            seeOnMap();
        }
    }

    private void seeOnMap() {
        // TODO make intent things visible on map
        mMap.clear();
        Intent i = getIntent();
        Double lt, lg;
        lt = i.getDoubleExtra("lat", 0);
        lg = i.getDoubleExtra("log", 0);
        MarkerOptions myMOptions = new MarkerOptions();
        if (i.getStringExtra("subtype") != null) {
            myMOptions.title("To here");
            myMOptions.position(new LatLng(lt,lg));
            mMap.addMarker(myMOptions);
            MarkerOptions fromHere = new MarkerOptions();
            lt = i.getDoubleExtra("flat",0);
            lg = i.getDoubleExtra("flog",0);
            fromHere.title("From here");
            fromHere.position(new LatLng(lt,lg));
            fromHere.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(fromHere);
        }
        else
        {
            myMOptions.title("Pick up from here");
            myMOptions.position(new LatLng(lt, lg));
            mMap.addMarker(myMOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lt,lg),10f));
        }
        selectBtn = findViewById(R.id.select_button_in_map);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    else if(identifier.equals("pickup"))
                        setResult(102,intent);
                    finish();
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {

                myMarker = latLng;
                myMOptions.position(latLng).title("Here");
                myMOptions.position(latLng);
                mMap.clear();
                mMap.addMarker(myMOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });

    }
}