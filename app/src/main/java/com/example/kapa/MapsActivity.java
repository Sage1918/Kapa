package com.example.kapa;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.kapa.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int REQUEST_CODE = 1002;
    public static final int DEFAULT_SPEED = 8;
    public static final int MAX_SPEED = 3;
    String drid;
    boolean driver;
    Marker driverMarker;
    // For Google maps
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private String type,user_id;
    private LatLng myMarker,toLoc;
    private FloatingActionButton selectBtn;
    // API for location Services
    FusedLocationProviderClient locationProvider;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = getIntent().getStringExtra("type");
        if(type.equals("ride")) {

            drid = getIntent().getStringExtra("drid");
            user_id = getIntent().getStringExtra("user_id");

            if (getIntent().getStringExtra("mode").equals("driver"))
            {
                driver = true;
            }
            else
                driver = false;
        }

        updateGPS();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * DEFAULT_SPEED);
        locationRequest.setFastestInterval(1000 * MAX_SPEED);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(driver)
                drive(locationResult.getLastLocation());
            }
        };
        locationProvider = LocationServices.getFusedLocationProviderClient(this);

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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(10.12345,76.12354),8f));
        mMap.clear();
        if (type.equals("select")) {
            mapMakeSelection();
        } else if (type.equals("seeOnMap")) {
            seeOnMap();
        } else if (type.equals("ride")) {
            Intent i = getIntent();
           toLoc = new LatLng(i.getDoubleExtra("tolat",0), i.getDoubleExtra("tolog",0));
            toDestMarker();
            if(driver) {
                startTracking();
            }
            else  //user is passenger
            {
                DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("location").child(drid);
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double lt = 0, lg = 0;
                        if (snapshot.exists()) {
                            for (DataSnapshot i : snapshot.getChildren())
                                if (i.getKey() == "lat")
                                    lt = i.getValue(double.class);
                                else if (i.getKey() == "log")
                                    lg = i.getValue(double.class);

                            myMarker = new LatLng(lt, lg);
                            startGetting();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

        }
    }


    private void seeOnMap() {
        // Done for Driver and some passenger make intent things visible on map
        mMap.clear();
        Intent i = getIntent();
        Double lt, lg;
        lt = i.getDoubleExtra("lat", 0);
        lg = i.getDoubleExtra("log", 0);
        MarkerOptions myMOptions = new MarkerOptions();
        if (i.getStringExtra("subtype") != null) {
            myMOptions.title("To here");
            myMOptions.position(new LatLng(lt, lg));
            mMap.addMarker(myMOptions);
            MarkerOptions fromHere = new MarkerOptions();
            lt = i.getDoubleExtra("flat", 0);
            lg = i.getDoubleExtra("flog", 0);
            fromHere.title("From here");
            fromHere.position(new LatLng(lt, lg));
            fromHere.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(fromHere);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lt, lg), 10f));
        } else {
            myMOptions.title("Pick up from here");
            myMOptions.position(new LatLng(lt, lg));
            mMap.addMarker(myMOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lt, lg), 10f));
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
                if (myMarker == null)
                    Toast.makeText(MapsActivity.this, "Please Select a place first", Toast.LENGTH_SHORT).show();
                else {
                    // Done return the x and y coordinates to the other activity
                    Intent intent = new Intent();
                    intent.putExtra("latitude", myMarker.latitude);
                    intent.putExtra("longitude", myMarker.longitude);

                    Intent haha = getIntent();
                    String identifier;
                    identifier = haha.getStringExtra("selecting");
                    if (identifier.equals("from"))
                        setResult(100, intent);
                    else if (identifier.equals("to"))
                        setResult(101, intent);
                    else if (identifier.equals("pickup"))
                        setResult(102, intent);
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

    private void startGetting() {
       // do {
            DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("location").child(drid);
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    double lt = 0, lg = 0;
                    if (snapshot.exists()) {
                        for (DataSnapshot i : snapshot.getChildren())
                            if (i.getKey().equals("lat"))
                                lt = i.getValue(double.class);
                            else if (i.getKey().equals("log"))
                                lg = i.getValue(double.class);

                            Log.d("SEE ME",String.valueOf(lt));
                        Log.d("SEE ME2",String.valueOf(lg));

                        myMarker = new LatLng(lt, lg);
                        if(driverMarker == null)
                        {
                            driverMarker = mMap.addMarker(new MarkerOptions().title("driver position").position(myMarker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myMarker,15f));
                        }
                        else
                        {
                            driverMarker.setPosition(myMarker);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myMarker));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
       // } while(myMarker.latitude - toLoc.latitude < 0.00001 && myMarker.longitude - toLoc.longitude <0.00001);
    }


    private void updateGPS() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Done: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this).setMessage("We need permission to ensure perfect user experience ;)")
                        .setCancelable(false)
                        .setPositiveButton("I see...", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapsActivity.this , new String[] {Manifest.permission.ACCESS_FINE_LOCATION}
                                , REQUEST_CODE);
                            }
                        }).show();
            }
            else
                ActivityCompat.requestPermissions(MapsActivity.this , new String[] {Manifest.permission.ACCESS_FINE_LOCATION}
                                , REQUEST_CODE);
            return;
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(locationProvider != null)
            locationProvider.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

                @Override
                public void onSuccess(Location location) {
                    if(location != null)
                    myMarker = new LatLng(location.getLatitude(),location.getLongitude());
                }
            });
        }
    }

    private void startTracking()
    {
        // Start tracking the location of the driver
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            locationProvider.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

    private void stopTracking()
    {
        // Stop tracking the location of the driver
        locationProvider.removeLocationUpdates(locationCallback);
    }

    private void drive(@NonNull Location l)
    {
        // Updates the real time database with the location of the user(who happens to be the driver). This mehod is only called if the user is a driver and not a passenger.
        DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("location").child(user_id);

        // Location model is a class that model how the location is stored in the database. Note: this class is defined in this java file itself(MapsActivity.java)
        myRef.setValue(new LocationModel(user_id,l.getLatitude(),l.getLongitude()));
        LatLng latLng ;
        latLng = new LatLng(l.getLatitude(),l.getLongitude());
        // If there is no marker for driver on map already
        if(driverMarker == null)
        {
            drawPath(latLng);
            MarkerOptions temp = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            driverMarker = mMap.addMarker(temp);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,8f));
        }
        else
        {
            driverMarker.setPosition(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f));
        }

        // toLoc is the location of the destination. This a CHEAP way of checking if the user has reached their destination, if yes stop tracking them.
        if(l.getLatitude() - toLoc.latitude < 0.00001 && l.getLongitude() - toLoc.longitude <0.00001)
            stopTracking();
    }

    private void drawPath(LatLng latLng) {
        // method to draw the route from start to finish if user is driver. Also finds distance travelled, calculates score and updates the database.
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination",String.valueOf(toLoc.latitude)+","+String.valueOf(toLoc.longitude))
                .appendQueryParameter("origin",String.valueOf(latLng.latitude)+","+String.valueOf(latLng.longitude))
                .appendQueryParameter("mode","driving")
                //Dirty solution, use strings or local properties
                .appendQueryParameter("key","AIzaSyAXMh5cETuF4lrHi8NyBKf-WeLbCC33nTU")
                .appendQueryParameter("units","metric")
                .toString();
        

    }

    private void toDestMarker()
    {
        // Adds the marker at the destination
        mMap.addMarker(new MarkerOptions().position(toLoc)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private List<LatLng> decodePolyline(String encoded)
    {
        /* The path as returned by the direction API will be compressed/shortened for easy transfer across the internet, the coordinates along the path
         are encoded in base64 format which needs to be decoded to get the actual coordinates(See documentation for more detailed info.
          This function takes in the encoded string(as found in the JSON returned by the API), decodes them and returns
          a list of coordinates making up the path.

          Credits to random stranger on the internet.*/

        List<LatLng> coordinatesOfPath = new ArrayList<>();
        int lat = 0,lng = 0;

        for(int i=0,len = encoded.length(); i < len;)
        {
            int b,shift = 0,result = 0;
            do{
                b = encoded.charAt(i++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }while(b >= 0x20);

            int temp;
            temp = ((result & 1) != 0 ? -(result >> 1) : (result >>1));
            lat += temp;

            shift = 0;
            result = 0;
            do{
                b = encoded.charAt(i++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            }while(b >= 0x20);

            temp = ((result & 1) != 0 ? -(result >> 1) : (result >>1));
            lng += temp;

            coordinatesOfPath.add(new LatLng((double)lat/1E5,(double)lng/1E5 ));
        }

        return coordinatesOfPath;
    }

    public class LocationModel
    {
        // Class that models how the location data is stored in the database.
        private String uid;
        private double lat;
        private double log;

        //! An empty constructor for firebase code... DO NOT REMOVE...
        LocationModel()
        {

        }

        public LocationModel(String uid, double lat, double log) {
            this.uid = uid;
            this.lat = lat;
            this.log = log;
        }

        public String getUid() {
            return uid;
        }

        public double getLat() {
            return lat;
        }

        public double getLog() {
            return log;
        }
    }

    /*
    // Deprecatated code... causes errors which are pain to remove.
    @Override
    protected void onStop() {
        if (type.equals("ride")) {
            super.onStop();

            Task<Void> myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("user")
                    .child(user_id).child("score").setValue(10);
            Task<Void> myRef2 = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("user")
                    .child(drid).child("score").setValue(10);

        }
    }
    */
    @Override
    public void onBackPressed()
    {
            // If user press back, don't let them go back to previous activity...
            if(backPressedTime + 2000 > System.currentTimeMillis())
            {
                super.onBackPressed();
                return;
            }
            else
                Toast.makeText(MapsActivity.this,"Press Back again to exit",Toast.LENGTH_SHORT).show();

            backPressedTime = System.currentTimeMillis();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Something to do with permission, modify at your own risk...
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    updateGPS();
                }
                else {
                    Toast.makeText(this, "Please provide permission", Toast.LENGTH_SHORT).show();
                }
        }

    }
}
