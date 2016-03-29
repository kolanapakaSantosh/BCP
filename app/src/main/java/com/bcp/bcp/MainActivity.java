package com.bcp.bcp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bcp.bcp.Geofencing.GeofenceIntentService;
import com.bcp.bcp.Geofencing.LocationIntentService;
import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.GeoFence;
import com.bcp.bcp.gcm.QuickstartPreferences;
import com.bcp.bcp.gcm.RegistrationIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, OnMapReadyCallback, LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationClient.OnAddGeofencesResultListener {
    private SwitchCompat switchCompat;
    private GoogleMap gmap;

    LocationManager locationManager;
    Criteria criteria;
    String bestProvider;
    Location location;
    double latitude;
    double longitude;
    boolean isInserted;

    GPSTracker gps;
    Credentials credentials;
    private SharedPreferences.Editor mEditor;


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;
    private ImageButton imageButton;
    DatabaseHandler databaseHandler;
    List<GeoFence> fenceList;

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchCompat = (SwitchCompat) findViewById(R.id.Switch);

        gps = new GPSTracker(MainActivity.this);
        SharedPreferences mSharedPreferences = getSharedPreferences("Shared", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        switchCompat.setChecked(mSharedPreferences.getBoolean("SWITCH", false));

        credentials = new Credentials();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

       /* MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);*/

        // initiating geofence service
        initGeofence();

        switchCompat.setOnCheckedChangeListener(this);

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }


        imageButton = (ImageButton)findViewById(R.id.imageButton);
        databaseHandler = new DatabaseHandler(this);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInserted = databaseHandler.addFence(new GeoFence("178.123", "78.123", "2", "DLF"));
                databaseHandler.addFence(new GeoFence("111.123", "18.123", "3", "CHN"));
                databaseHandler.addFence(new GeoFence("146.123","98.123","2","KOC"));
                if (isInserted) {
                    Log.d("Insert: ", "Inserted ..");
                    Toast.makeText(getApplicationContext(),"Fences Added",Toast.LENGTH_LONG).show();
                    fenceList = databaseHandler.getAllGeoFence();
                    for(GeoFence geoFence : fenceList){
                        String log = "Id: "+geoFence.getId()+" ,Lattitude: " + geoFence.getLat() + " ,Longittude: " +geoFence.getLng() + "  ,Radius: " +geoFence.getRadius() +" ,FenceName: " +
                                geoFence.getFenceName();
                        // Writing Contacts to log
                        Log.d("Fences: ", log);
                    }
                }
            }
        });
    }


    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.Switch:
                if (isChecked) {
                    new UploadToFTAsync(UploadToFTAsync.getConfigTime, null, this).execute();
                    mEditor.putBoolean("SWITCH", true);
                    mEditor.commit();
                } else {
                    try {
                        Intent intent = new Intent(this, MyLocationService.class);
                        stopService(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Your Location is default", Toast.LENGTH_LONG).show();
                    mEditor.putBoolean("SWITCH", false);
                    mEditor.commit();
                }
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);

    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(latitude, longitude);
        gmap.addMarker(new MarkerOptions().position(latLng).title("My Location").snippet("Track Me"));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        gmap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void initGeofence() {
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            locationClient = new LocationClient(this, this, this);
            locationClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5 * 50 * 1000);
        locationRequest.setFastestInterval(5 * 50 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Intent intent = new Intent(this, LocationIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);
        locationClient.requestLocationUpdates(locationRequest, pendingIntent);

        ArrayList<Store> storeList = getStoreList();
        if (null != storeList && storeList.size() > 0) {
            ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();
            for (Store store : storeList) {
                float radius = (float) store.radius;
                Geofence geofence = new Geofence.Builder()
                        .setRequestId(store.id)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setCircularRegion(store.latitude, store.longitude, radius)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build();

                geofenceList.add(geofence);
            }
            PendingIntent geoFencePendingIntent = PendingIntent.getService(this, 0,
                    new Intent(this, GeofenceIntentService.class), PendingIntent.FLAG_UPDATE_CURRENT);
            locationClient.addGeofences(geofenceList, geoFencePendingIntent, this);

        }
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
        if (LocationStatusCodes.SUCCESS == i) {
            //todo check geofence status
            Log.e("geofence status ",i+"");
        } else {

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private ArrayList<Store> getStoreList() {
        ArrayList<Store> storeList = new ArrayList<Store>();
        for (int i = 0; i < 1; i++) {
            Store store = new Store();
            store.id = String.valueOf(i);//17.458732, 78.372670
            store.address = "Google";
            store.latitude = 17.458732;
            store.longitude = 78.372670;
            store.radius = 100.0D;

            storeList.add(store);
        }

        return storeList;
    }

    public class Store {
        String id;
        String address;
        double latitude;
        double longitude;
        double radius;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != locationClient) {
            locationClient.disconnect();
        }
    }
}
