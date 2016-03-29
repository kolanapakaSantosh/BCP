package com.bcp.bcp;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.api.services.drive.Drive;
import com.google.api.services.fusiontables.Fusiontables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anjup on 3/10/16.
 */
public class MyLocationService extends Service {


    private static final String TAG = "HelloService";

    private long delay = 2000;
    private Handler handler;
    private GPSTracker gps;
    private double latitude = 0, longitude = 0;
    private Credentials credentials;
    private Runnable runnable;

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        Toast.makeText(getApplicationContext(), "Tracking Started", Toast.LENGTH_LONG).show();

        handler = new Handler();
        credentials = new Credentials();
        final SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("Shared", Context.MODE_PRIVATE);

        runnable = new Runnable() {
            @Override
            public void run() {
                delay = mSharedPreferences.getLong("CONFIG TIME", 60000);
                handler.postDelayed(this, delay);
                gpsTracker();
                Log.e("MyService", "delay = " + delay);
            }
        };

        handler.post(runnable);

        return super.onStartCommand(intent, flags, startId);
    }



    public void gpsTracker() {
        gps = new GPSTracker(MyLocationService.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Log.e("latitude", "" + latitude);
            Log.e("longitude", "" + longitude);
            if(latitude != 0 && longitude != 0){
                File file = credentials.saveFile(latitude, longitude,getApplicationContext());
                new UploadToFTAsync(UploadToFTAsync.uploadFile, file, getApplicationContext()).execute();
            }

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service stopped", "Service stopped");
        try {
            handler.removeCallbacks(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        };
    }
}
