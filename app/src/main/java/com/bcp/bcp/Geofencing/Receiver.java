package com.bcp.bcp.Geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bcp.bcp.R;

public class Receiver extends BroadcastReceiver {

	Context con;
	SharedPreferences prefs;
	BroadcastReceiver receiver;
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		con = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(con);
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.getApplicationContext().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_TYPE);
				Log.e("Receiver",""+networkInfo.getType());
				if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
				{
					ConnectivityManager myConnManager = (ConnectivityManager) con.getSystemService(con.CONNECTIVITY_SERVICE);
					NetworkInfo myNetworkInfo = myConnManager.getActiveNetworkInfo();
					WifiManager myWifiManager = (WifiManager)con.getSystemService(Context.WIFI_SERVICE);
					WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
					if (myNetworkInfo.isConnected())
					{
						Log.e("Receiver","wifissid : " + myWifiInfo.getSSID() );
						//String ssid = myWifiInfo.getSSID().contains(cs)
						if(myWifiInfo.getSSID().contains(con.getResources().getString(R.string.ssid)))
						{
							//Toast.makeText(con, "Connected", 100).show();
							//Log.e("Receiverrr","connected to Google Guestpsk");
							//new submit_logindetails().execute(); // to save the login details
							con.unregisterReceiver(this);
						}

					}
				}

			}
		}, filter);

	}
	




}
