package com.bcp.bcp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.FusiontablesScopes;
import com.google.api.services.fusiontables.model.Sqlresponse;

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
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by anjup on 3/10/16.
 */
//test1
public class Credentials {

    private static final JsonFactory JSON_FACTORY = com.google.api.client.extensions.android.json.AndroidJsonFactory.getDefaultInstance();
    private static HttpTransport sHttpTransport;
    private GoogleCredential googleCredential;
    private static final String MY_APP_NAME = "My App Name";
    private Fusiontables mFusionTable;
    private Drive mDrive;

    public void initializeHttpTransport() {
        try {
            sHttpTransport = com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport();
        } catch (Exception e) {

        }
    }

    public void initializeGoogleCredential() {
        initializeHttpTransport();
        googleCredential = (GoogleCredential) getCredential();
        if (googleCredential != null) {
            mFusionTable = new Fusiontables.Builder(sHttpTransport, JSON_FACTORY, googleCredential).setApplicationName(MY_APP_NAME).build();

            mDrive = new Drive.Builder(sHttpTransport, JSON_FACTORY, googleCredential).setApplicationName(MY_APP_NAME).build();
        }

    }

    public File saveFile(double latitude, double longitude,Context mContext) {
        String lat = String.valueOf(latitude);
        String lan = String.valueOf(longitude);
        String email = "";
        Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(mContext).getAccounts();

        for (Account account : accounts) {

            if (gmailPattern.matcher(account.name).matches()) {

                email = account.name;

            }

        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date1 = new Date();
        String date = dateFormat.format(date1);

        String textToSave = lat + "," + lan + "," + email + "," + date;
        File myFile = null;
        try {
            myFile = new File("/sdcard/myFile");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(textToSave);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return myFile;
    }

    public void insertIntoFusionTables(File file) {
        initializeGoogleCredential();
        String tableId;
        try {
            tableId = "1GF89Dxwq5m19T8Scx-zRFpaEtVubUfN2cRUZO5cJ";
            mDrive.permissions().create(tableId, getPermission()).execute();

            mFusionTable.table().importRows(tableId,
                    new FileContent("application/octet-stream", file)).execute();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public long getTimeFromConfigTable() {
        initializeGoogleCredential();
        try {
            String tableId = "1GF89Dxwq5m19T8Scx-zRFpaEtVubUfN2cRUZO5cJ";
            mDrive.permissions().create(tableId, getPermission()).execute();
            Sqlresponse sqlresponse = mFusionTable.query().sql("SELECT * FROM " + tableId).execute();
            Log.i("Time Duration" , " in Milliseconds " + sqlresponse.getRows().get(0).get(0));
            return Long.parseLong(sqlresponse.getRows().get(0).get(0).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 60000;
    }

    private Permission getPermission() {
        /**
         * 'reader', 'commenter', 'writer', and 'owner'
         */
        Permission permission = new Permission();
        permission.setEmailAddress("anju.thanal@gmail.com");
        permission.setType("user");
        permission.setRole("reader");
        permission.setRole("writer");
        return permission;
    }


    private Credential getCredential() {
        try {
            String p12Password = "notasecret";

            ClassLoader classLoader = MainActivity.class.getClassLoader();

            KeyStore keystore = KeyStore.getInstance("PKCS12");
            InputStream keyFileStream = classLoader.getResourceAsStream("BCP PROJECT-64c714454406.p12");

            if (keyFileStream == null){
                throw new Exception("Key File Not Found.");
            }

            keystore.load(keyFileStream, p12Password.toCharArray());
            PrivateKey key = (PrivateKey)keystore.getKey("privatekey", p12Password.toCharArray());

            return new GoogleCredential.Builder()
                    .setTransport(sHttpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId("bcp-project-1239@appspot.gserviceaccount.com")
                    .setServiceAccountPrivateKey(key)
                    .setServiceAccountScopes(Arrays.asList(FusiontablesScopes.FUSIONTABLES, DriveScopes.DRIVE))
                    .build();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
