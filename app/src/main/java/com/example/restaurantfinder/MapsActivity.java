package com.example.restaurantfinder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.restaurantfinder.databinding.ActivityMapsBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static final int Request_User_Location_Code = 99;
    private double latitude, longitude;
    private RequestQueue myQueue;
    private int proximityDistance = 10000;
    public ArrayList<String> nameList = new ArrayList<String>();
    public ArrayList<String> randNameList = new ArrayList<String>();

    public int receivedDistanceMiles;
    public int receivedMinPrice;
    public int receivedMaxPrice;
    ArrayList<String> receivedCheckYes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkUserLocationPermission();
        }

        myQueue = Volley.newRequestQueue(this);

        // Receiving data from MainActivity.java and assigning them to variables
        Intent receiverIntent = getIntent();
        receivedDistanceMiles = receiverIntent.getIntExtra("DISTANCE_SENDER",0);
        receivedMinPrice = receiverIntent.getIntExtra("MINIMUM_PRICE_SENDER", 0);
        receivedMaxPrice = receiverIntent.getIntExtra("MAXIMUM_PRICE_SENDER", 0);
        receivedCheckYes = receiverIntent.getStringArrayListExtra("CHECK_YES_LIST");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurant:
                mMap.clear();
                getDeviceLocation();

                milesToMeters();
                String url = getUrl(latitude, longitude, "restaurant");

                // Parse Through JSON method
                Toast.makeText(this, "Searching for Nearby Restaurants...", Toast.LENGTH_SHORT).show();
                ParseThroughJSON(url);

                RandomizeList();
                break;
        }
    }

    public void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.
                getLastLocation(googleApiClient);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    public void ParseThroughJSON(String url) {
        // Code used from "Android Studio - Parse JSON url (2022) from YouTube"
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject restaurant = jsonArray.getJSONObject(i);
                                String restName = restaurant.getString("name");
                                nameList.add(restName);

                                double lat = Double.parseDouble(restaurant.getJSONObject("geometry")
                                        .getJSONObject("location")
                                        .getString("lat"));

                                double lng = Double.parseDouble(restaurant.getJSONObject("geometry")
                                        .getJSONObject("location")
                                        .getString("lng"));

                                Log.d("MapsActivity", "Name: " + restName +
                                        "; Latitude: " + lat +
                                        ", Longitude: " + lng);

                                LatLng latLng = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(latLng).title(restName));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        myQueue.add(request);

        /*try {

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create(mediaType, )
            Request request = new Request.Builder()
                    .url(s)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();

            ArrayList<String> list = new ArrayList<String>();
            JSONObject obj = new JSONObject(response.body().string());
            JSONArray array = obj.getJSONArray("results");

            for (int i = 0; i < array.length(); i++){
                //list.add(array.getJSONObject(i).getString("name"));
                Log.d("MapsActivity", "Name: " + array.getJSONObject(i).getString("name"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    // Code not used
    public void RandomizeList() {
        /*ArrayList<String> list = new ArrayList<String>();
        list = nameList;
        Collections.shuffle(list);

        // Keep the first 8 in list
        for (int i = 0; i < 10; i++){
            randNameList.add(list.get(i));
            Log.d("RANDOMIZE", "List item #" + i + ": " + list.get(i));
        }*/
    }

    private String getUrl(double latitude, double longitude, String placeType) {
        StringBuilder googleURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location=" + latitude + "," + longitude);
        googleURL.append("&radius=" + proximityDistance);
        googleURL.append("&type=" + placeType);
        googleURL.append("&sensor=true");
        googleURL.append("&key=" + "AIzaSyCuRHUmGWSu6Rx3eVCDddj7qSFeaOWxczA");

        Log.d("MapsActivity", "url = " + googleURL);
        Log.d("MapsActivity", "Latitude = " + latitude);
        Log.d("MapsActivity", "Longitude = " + longitude);

        return googleURL.toString();
    }

    public void milesToMeters() {
        proximityDistance = receivedDistanceMiles * 1609;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean checkUserLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (googleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "Permission Denied...", Toast.LENGTH_SHORT).show();
                }

                return;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        /*latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastLocation = location;

        if (currentUserLocationMarker != null){
            currentUserLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLatitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("User Current Location:");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

        if (googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}