package com.example.chenminyao.resaas_assessment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ListingListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ListingDownloadClient listingDownloadClient;
    private GcmNetworkManager mGcmNetworkManager;
    private ArrayList<Listing> shopListings;
    private LocationManager locationManager;
    private Location mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (!checkPermission(this))
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        Intent intent = getIntent();
        shopListings = new ArrayList<>();
        if (intent != null) {
            parseResult(getIntent().getStringExtra("result"));
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a Linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
//        listingDownloadClient = new ListingDownloadClient(this,this);
//        listingDownloadClient.getListings(-123.120177,49.280392);
        mAdapter = new RecyclerViewAdapter(shopListings, this);
        mRecyclerView.setAdapter(mAdapter);
        mGcmNetworkManager = GcmNetworkManager.getInstance(this);
        PeriodicTask task = new PeriodicTask.Builder()
                .setService(MyLocationService.class)
                .setTag(MyLocationService.TASK_GET_LOCATION_PERIODIC)
                .setPeriod(30L)
                .setPersisted(true)
                .build();
        mGcmNetworkManager.schedule(task);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh:
//                    OneoffTask task = new OneoffTask.Builder()
//                            .setTag(MyLocationService.TASK_GET_LOCATION_ONCE)
//                            .setExecutionWindow(0L, 1L)
//                            .setService(MyLocationService.class)
//                            .build();
//                    mGcmNetworkManager.schedule(task);
                getLocation();
                //TODO getCurrentLocation, allow user refresh and get location immediately
                break;
            case R.id.enter:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View enter = inflater.inflate(R.layout.enter_lat_lon, null);
                final EditText lat = (EditText) enter.findViewById(R.id.et_lat);
                final EditText lon = (EditText) enter.findViewById(R.id.et_lon);
                final AlertDialog enterDialog;
                enterDialog = new AlertDialog.Builder(this)
                        .setTitle("Enter latitude and longitude")
                        .setPositiveButton("get", null)
                        .setView(enter)
                        .create();
                enterDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(final DialogInterface dialog) {

                        Button button = enterDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        if (button != null) {
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    listingDownloadClient = new ListingDownloadClient(MainActivity.this, MainActivity.this);
                                    listingDownloadClient.getListings(Double.parseDouble(lon.getText().toString()), Double.parseDouble(lat.getText().toString()));
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
                enterDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void parseResult(String result) {
//        String result = getIntent().getStringExtra("result");
        if (result == null || result.length() == 0) return;
        shopListings.clear();
        try {
            JSONObject response = new JSONObject(result);
            JSONArray results = response.getJSONArray("restaurants");
            Log.d("Response", results.toString());
            for (int i = 0; i < results.length(); i++) {
                JSONObject restaurantsObject = results.getJSONObject(i);
                JSONObject restaurantObject = restaurantsObject.getJSONObject("restaurant");
                Listing restaurant = new Listing();
                restaurant.setName(restaurantObject.getString("name"));
                JSONObject location = restaurantObject.getJSONObject("location");
                restaurant.setAddress(location.getString("address"));
                JSONObject userRating = restaurantObject.getJSONObject("user_rating");
                restaurant.setUserRating(userRating.getString("aggregate_rating"));
                shopListings.add(restaurant);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadFinished(String result) {
        parseResult(result);
        mAdapter = new RecyclerViewAdapter(shopListings, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    // check whether we are having location permission for marshmellow
    public static boolean checkPermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission
                .ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;

        }
    }

    private void getLocation() {
        Location lastKnownGPSLocation;
        Location lastKnownNetworkLocation;
        String gpsLocationProvider = LocationManager.GPS_PROVIDER;
        String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            lastKnownNetworkLocation = locationManager.getLastKnownLocation(networkLocationProvider);
            lastKnownGPSLocation = locationManager.getLastKnownLocation(gpsLocationProvider);

            if (lastKnownGPSLocation != null) {
                this.mCurrentLocation = lastKnownGPSLocation;
            } else if (lastKnownNetworkLocation != null) {
                this.mCurrentLocation = lastKnownNetworkLocation;
            } else {
                return;
            }

            ListingDownloadClient listingDownloadClient = new ListingDownloadClient(MyApplication.getAppContext(), this);
            listingDownloadClient.getListings(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());

//            LocationChangedEvent event = new LocationChangedEvent();
//            event.setLocation(mCurrentLocation);
//
//            EventHelper.publishEvent(event);

        } catch (SecurityException sex) {
        }
    }
}
