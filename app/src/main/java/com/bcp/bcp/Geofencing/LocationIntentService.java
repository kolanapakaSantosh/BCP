package com.bcp.bcp.Geofencing;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bcp.bcp.MainActivity;
import com.bcp.bcp.R;
import com.google.android.gms.location.LocationClient;

/**
 * Created by santosh kolanapaka on 03/28/16.
 */
public class LocationIntentService extends IntentService {
    public static final String LOCATION_UPDATE_INTENT_SERVICE = "LocationIntentService";

    public LocationIntentService() {
        super(LOCATION_UPDATE_INTENT_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Location location = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);

        generateNotification("Geofence Breach", location.getLatitude() + " " + location.getLongitude());
    }

    private void generateNotification(String title, String content) {
        long when = System.currentTimeMillis();
        Intent notifyIntent = new Intent(this, MainActivity.class);//ActivityRecognitionActivity.class);//MainActivity.class);
        notifyIntent.putExtra("title", title);
        notifyIntent.putExtra("content", content);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setWhen(when);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) when, builder.build());
    }
}
