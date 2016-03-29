package com.bcp.bcp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;


/**
 * Created by anjup on 3/4/16.
 */
public class UploadToFTAsync extends AsyncTask<Void,Void,Void> {

    private SharedPreferences.Editor mEditor;
    private ProgressDialog progressDialog;
    private android.content.Context mContext;
    private File file;
    Credentials credentials;
    private int todo;

    public static final int uploadFile = 1;
    public static final int getConfigTime = 2;


    UploadToFTAsync(int todo, File file, Context mContext) {
        this.todo = todo;
        this.mContext = mContext;
        this.file = file;
        credentials = new Credentials();
        if (mContext != null) {
            SharedPreferences mSharedPreferences = mContext.getSharedPreferences("Shared", Context.MODE_PRIVATE);
            mEditor = mSharedPreferences.edit();
        }
    }


    @Override
    protected Void doInBackground(Void... params){
        switch (todo) {
            case uploadFile:
                credentials.insertIntoFusionTables(file);
                break;
            case getConfigTime:
                long configurableTime = credentials.getTimeFromConfigTable();
                mEditor.putLong("CONFIG TIME", configurableTime);
                mEditor.commit();
                break;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mContext != null && todo == getConfigTime) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (todo == getConfigTime) {
            Intent intent = new Intent(mContext, MyLocationService.class);
            mContext.startService(intent);
        }else{
            if(mContext!=null){
                Toast.makeText(mContext, "Location updated to fution table ", Toast.LENGTH_LONG).show();
            }

        }
        //progressDialog.dismiss();
    }



}
