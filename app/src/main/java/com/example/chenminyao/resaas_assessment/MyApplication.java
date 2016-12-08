package com.example.chenminyao.resaas_assessment;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

/**
 * Created by chenminyao on 2016-12-07.
 */

public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
