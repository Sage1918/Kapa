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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int REQUEST_CODE = 1002;
    public static final int DEFAULT_SPEED = 8;
    public static final int MAX_SPEED = 3;
    public static final double MULTIPLIER_PER_DISTANCE = 0.25;
    String drid;
    boolean driver;
    Marker driverMarker;
    // For Google maps
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private String type,user_id;
    // Note: because of few unforeseen reasons the variable myMarker will give location of user only if user is driver,
    // in case user is passenger, use psngLoc to get the location of user...
    private LatLng myMarker,toLoc,psngLoc;
    private FloatingActionButton selectBtn;
    // API for location Services
    FusedLocationProviderClient locationProvider;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    RequestQueue requestQueue;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        type = getIntent().getStringExtra("type");
        if(type.equals("ride")) {

            drid = getIntent().getStringExtra("drid");
            user_id = getIntent().getStringExtra("user_id");

            if (getIntent().getStringExtra("mode").equals("driver"))
            {
                driver = true;
            }
            else {
                driver = false;
            }
        }

        updateGPS();

        // Initialising request queue for giving requests to the Direction API to get distance and directions...
        // Request Queue, in the literal sense a queue of requests that the app gives...
        requestQueue = Volley.newRequestQueue(this);

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
            if(driver) {
                toDestMarker();
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
                                if (i.getKey().equals("lat"))
                                    lt = i.getValue(double.class);
                                else if (i.getKey().equals("log"))
                                    lg = i.getValue(double.class);

                            Log.d("locationRec",String.valueOf(lt) + " " + String.valueOf(lg));
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
        /*Start Getting the location of Driver so that passenger map can be updated... */

        // method updateGPS() initially sets the myMarker value with the location of the user.
        passengerMarker(psngLoc);


        // Java is Weird...
        final boolean[] passengerOnBoard = {false};
        final boolean[] timeToStop = {false};

        List<LatLng> passengerLocs = new ArrayList<>();

       do {
            DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("location").child(drid);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
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


                        double myLat = psngLoc.latitude;
                        double myLog = psngLoc.longitude;
                        Log.d("PasssengerLoc",String.valueOf(myLat) + " " + String.valueOf(myLog));

                        myMarker = new LatLng(lt, lg);
                        // Update the map on passenger side...
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

                        // Checking if the passenger got on board, If the driver and passenger are close enough, then assume that the passenger got on board...
                        // This is a Bad way of checking btw...
                        if(Math.abs(myLat - myMarker.latitude) < 0.00001 && Math.abs(myLog - myMarker.longitude) < 0.00001) {
                            if (!passengerOnBoard[0])
                            {
                                passengerOnBoard[0] = true;
                                passengerLocs.add(myMarker);
                            }
                        }
                        // If the distance between Driver and Passenger increases after the passenger got on board then assume that the passenger got off...
                        else
                            if(passengerOnBoard[0]) {
                                timeToStop[0] = true;
                                passengerLocs.add(myMarker);
                                setScore(passengerLocs);
                            }

                        // This might cause Async problems...
                        updateGPS();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //Dirty Solution...
                }
            });

       } while(!timeToStop[0]);
    }


    private void updateGPS() {

        // if Location Permission is not given...
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Done: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // If No permission, then Request Permission...
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
        }
        // If permission already given, then set myMarker with current location.
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(locationProvider != null)
            locationProvider.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

                @Override
                public void onSuccess(Location location) {
                    if(location != null)
                        if(driver)
                            myMarker = new LatLng(location.getLatitude(),location.getLongitude());
                        // If not driver, We'll use a different variable to prevent Async problems...
                        else
                            psngLoc = new LatLng(location.getLatitude(),location.getLongitude());
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
        // Updates the real time database with the location of the user(who happens to be the driver). This method is only called if the user is a driver and not a passenger.
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
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f));
        }

        // toLoc is the location of the destination. This a CHEAP way of checking if the user has reached their destination, if yes stop tracking them.
        // Note: The lattitude precision is not calibrated according to the longitude of the user. Since the equator is the entire circumference of earth,
        // lattitude approximation taken here represent worst case scenario where the person is near the equator. For users further away from equator this
        // will only result in greater lattitude precision and theoretically, no problems should arise out of this...
        if(l.getLatitude() - toLoc.latitude < 0.0001 && l.getLongitude() - toLoc.longitude <0.0001)
            stopTracking();
    }

    private void drawPath(LatLng latLng) {
        // method to draw the route from start to finish if user is driver. Also finds distance travelled, calculates score and updates the database.
        // Creating the URL to give request...
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination",String.valueOf(toLoc.latitude)+", "+String.valueOf(toLoc.longitude))
                .appendQueryParameter("origin",String.valueOf(latLng.latitude)+", "+String.valueOf(latLng.longitude))
                .appendQueryParameter("mode","driving")
                .appendQueryParameter("units","metric")
                //Dirty solution, use strings or local properties
                .appendQueryParameter("key","AIzaSyAXMh5cETuF4lrHi8NyBKf-WeLbCC33nTU")
                .toString();

        // Creating a JSON object Request
        JsonObjectRequest myJsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // if the "status" in the JSON returned by API is OK, that is nothing went WRONG...
                    // See documentation to see more detailed description of json returned by API.
                    if(response.getString("status").equals("OK"))
                    {
                        // JSON array routes in JSON object response, there may be multiple routes(in our case its mostly 1)
                        JSONArray routes = response.getJSONArray("routes");

                        // A list to store the list of LatLng returned by the decodePolyline method and a polyline to add to map initially its null.
                        List<LatLng> points;
                        PolylineOptions myPolyLine = null;

                        for(int i=0; i<routes.length();++i)
                        {
                            points = new ArrayList<>();
                            myPolyLine = new PolylineOptions();

                            // A leg is direction from 1 point to another given in the request. This is more prominent when waypoints are used.
                            // waypoints are points that are compulsory included in the directions.
                            // if the request given to API is from location A to B with waypoints C and D, we'll have 3 legs(A->C, C->D and D->B).
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            for(int j=0; j<legs.length();++j)
                            {
                                // distance is a JSON object in each leg which given the distance. it has 2 fields: text and value. value is given in meters.
                                JSONObject distance = legs.getJSONObject(j).getJSONObject("distance");
                                setScore(distance.getString("value"));
                                JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");

                                for(int k=0;k<steps.length();++k)
                                {
                                    String strPolyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                    points = decodePolyline(strPolyline);
                                }
                            }

                            myPolyLine.addAll(points);
                            myPolyLine.width(10);
                            myPolyLine.geodesic(true);
                        }

                        mMap.addPolyline(myPolyLine);
                    }
                }
                catch (JSONException e)
                {
                    Toast.makeText(MapsActivity.this, "Something went WRONG...", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, "Error in requesting Directions...", Toast.LENGTH_SHORT).show();
            }
        });

        // Retry policies...
        RetryPolicy retryPolicy = new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        myJsonRequest.setRetryPolicy(retryPolicy);
        // Adding my Request to the Request Queue...
        requestQueue.add(myJsonRequest);
    }

    private void setScore(String value) {
        // For Driver...
        int distance = Integer.parseInt(value);
        DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("user");
        Query query = myRef.child(user_id).orderByChild("score");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double oldScore = snapshot.getValue(Double.class);
                myRef.child(user_id).child("score").setValue(oldScore + distance * MULTIPLIER_PER_DISTANCE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void setScore(List<LatLng> p)
    {
        // For the passenger...
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination",String.valueOf(p.get(1).latitude)+", "+String.valueOf(p.get(1).longitude))
                .appendQueryParameter("origin",String.valueOf(p.get(0).latitude)+", "+String.valueOf(p.get(0).longitude))
                .appendQueryParameter("mode","driving")
                //Dirty solution, use strings or local properties
                .appendQueryParameter("key","AIzaSyAXMh5cETuF4lrHi8NyBKf-WeLbCC33nTU")
                .appendQueryParameter("units","metric")
                .toString();
        JsonObjectRequest myJsonObjectReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");
                        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                        JSONObject distance = legs.getJSONObject(0).getJSONObject("distance");
                        int numDistance = distance.getInt("value");

                        DatabaseReference myRef = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("user");
                        Query query = myRef.child(user_id).orderByChild("score");
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                double oldScore = snapshot.getValue(double.class);
                                myRef.child(user_id).child("score").setValue(oldScore + numDistance * MULTIPLIER_PER_DISTANCE);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                catch (JSONException e)
                {
                    Log.e("JSONERRor",e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Retry policies...
        RetryPolicy retryPolicy = new DefaultRetryPolicy(10000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        myJsonObjectReq.setRetryPolicy(retryPolicy);
        // add to queue...
        requestQueue.add(myJsonObjectReq);
    }

    private void toDestMarker()
    {
        // Adds the marker at the destination
        mMap.addMarker(new MarkerOptions().title("Destination").position(toLoc)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    private void passengerMarker(LatLng i)
    {
        mMap.addMarker(new MarkerOptions().title("My position").position(i));
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
