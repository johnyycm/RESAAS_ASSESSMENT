package com.example.chenminyao.resaas_assessment;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import java.util.ArrayList;

/**
 * Created by chenminyao on 2016-12-07.
 */

public class MyLocationService extends GcmTaskService implements ListingListener {
    private static final String TAG = MyLocationService.class.getSimpleName();

    private LocationManager locationManager;
    private Location mCurrentLocation;

    public static final String TASK_GET_LOCATION_ONCE = "location_oneoff_task";
    public static final String TASK_GET_LOCATION_PERIODIC = "location_periodic_task";


    private static final int RC_PLAY_SERVICES = 123;

//    @Override
//    public void onInitializeTasks() {
//        // When your package is removed or updated, all of its network tasks are cleared by
//        // the GcmNetworkManager. You can override this method to reschedule them in the case of
//        // an updated package. This is not called when your application is first installed.
//        //
//        // This is called on your application's main thread.
//        startPeriodicLocationTask(TASK_GET_LOCATION_PERIODIC,
//                30L, null);
//    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.d(TAG, "onRunTask: " + taskParams.getTag());

        String tag = taskParams.getTag();
        Bundle extras = taskParams.getExtras();
        // Default result is success.
        int result = GcmNetworkManager.RESULT_SUCCESS;

//        switch (tag) {
//            case TASK_GET_LOCATION_ONCE:
//                getLastKnownLocation();
//                break;
//
//            case TASK_GET_LOCATION_PERIODIC:
//                getLastKnownLocation();
//                break;
//
//        }
        getLastKnownLocation();

        return result;
    }


    public void getLastKnownLocation() {
        Location lastKnownGPSLocation;
        Location lastKnownNetworkLocation;
        String gpsLocationProvider = LocationManager.GPS_PROVIDER;
        String networkLocationProvider = LocationManager.NETWORK_PROVIDER;

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            lastKnownNetworkLocation = locationManager.getLastKnownLocation(networkLocationProvider);
            lastKnownGPSLocation = locationManager.getLastKnownLocation(gpsLocationProvider);

            if (lastKnownGPSLocation != null) {
                Log.i(TAG, "lastKnownGPSLocation is used.");
                this.mCurrentLocation = lastKnownGPSLocation;
            } else if (lastKnownNetworkLocation != null) {
                Log.i(TAG, "lastKnownNetworkLocation is used.");
                this.mCurrentLocation = lastKnownNetworkLocation;
            } else {
                Log.e(TAG, "lastLocation is not known.");
                return;
            }

            ListingDownloadClient listingDownloadClient = new ListingDownloadClient(MyApplication.getAppContext(), this);
            listingDownloadClient.getListings(mCurrentLocation.getLongitude(), mCurrentLocation.getLatitude());

//            LocationChangedEvent event = new LocationChangedEvent();
//            event.setLocation(mCurrentLocation);
//
//            EventHelper.publishEvent(event);

        } catch (SecurityException sex) {
            Log.e(TAG, "Location permission is not granted!");
        }

        return;
    }

    public static void startOneOffLocationTask(String tag, Bundle extras) {
        Log.d(TAG, "startOneOffLocationTask");

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MyApplication.getAppContext());
        OneoffTask.Builder taskBuilder = new OneoffTask.Builder()
                .setService(MyLocationService.class)
                .setTag(tag);

        if (extras != null) taskBuilder.setExtras(extras);

        OneoffTask task = taskBuilder.build();
        mGcmNetworkManager.schedule(task);
    }

    public static void startPeriodicLocationTask(String tag, Long period, Bundle extras) {
        Log.d(TAG, "startPeriodicLocationTask");

        GcmNetworkManager mGcmNetworkManager = GcmNetworkManager.getInstance(MyApplication.getAppContext());
        PeriodicTask.Builder taskBuilder = new PeriodicTask.Builder()
                .setService(MyLocationService.class)
                .setTag(tag)
                .setPeriod(period)
                .setPersisted(true)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED);

        if (extras != null) taskBuilder.setExtras(extras);

        PeriodicTask task = taskBuilder.build();
        mGcmNetworkManager.schedule(task);
    }


    public static boolean checkPlayServicesAvailable(Activity activity) {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(MyApplication.getAppContext());

        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to resolve the error.
                availability.getErrorDialog(activity, resultCode, RC_PLAY_SERVICES).show();
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void downloadFinished(String result) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("result",result);

// Because clicking the notification opens a new ("special") activity, there's
// no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("RESAAS notification")
                        .setAutoCancel(true)
                        .setContentText("Found Restaurant nearby, check them out!");

        mBuilder.setContentIntent(resultPendingIntent);


// Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}