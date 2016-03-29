package com.bcp.bcp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anjup on 3/22/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "geofenceManager";
    private static final String TABLE_GEOFENCE = "geofence";
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_FENCE_NAME = "fencename";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GEOFENCE_TABLE = "CREATE TABLE " + TABLE_GEOFENCE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_LAT + " TEXT, " + KEY_LNG + " TEXT,"+ KEY_RADIUS + " TEXT," + KEY_FENCE_NAME + " TEXT" + ")";
        db.execSQL(CREATE_GEOFENCE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCE);
        // Create tables again
        onCreate(db);

    }

    public boolean addFence(GeoFence geoFence){

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        boolean isInserted = false;

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, geoFence.getLat());
        values.put(KEY_LNG, geoFence.getLng());
        values.put(KEY_RADIUS, geoFence.getRadius());
        values.put(KEY_FENCE_NAME, geoFence.getFenceName());

        if(values!=null) {
            // Inserting Row
            sqLiteDatabase.insert(TABLE_GEOFENCE, null, values);
            isInserted = true;
        }
        //2nd argument is String containing nullColumnHack
        sqLiteDatabase.close(); // Closing database connection

        return isInserted;
    }

    public GeoFence getGeoFence(int id){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_GEOFENCE, new String[]{KEY_ID, KEY_LAT, KEY_LNG, KEY_RADIUS, KEY_FENCE_NAME}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        GeoFence geoFence = new GeoFence(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        // return contact

        return geoFence;
    }

    public List<GeoFence> getAllGeoFence() {
        List<GeoFence> geoFenceList = new ArrayList<GeoFence>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_GEOFENCE;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GeoFence geoFence = new GeoFence();
                geoFence.setId(Integer.parseInt(cursor.getString(0)));
                geoFence.setLat(cursor.getString(1));
                geoFence.setLng(cursor.getString(2));
                geoFence.setRadius(cursor.getString(3));
                geoFence.setFenceName(cursor.getString(4));

                // Adding contact to list
                geoFenceList.add(geoFence);
            } while (cursor.moveToNext());
        }

        // return contact list
        return geoFenceList;
    }

    public int updateGeoFence(GeoFence geoFence) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, geoFence.getLat());
        values.put(KEY_LNG, geoFence.getLng());
        values.put(KEY_RADIUS, geoFence.getRadius());
        values.put(KEY_FENCE_NAME, geoFence.getFenceName());

        // updating row
        return sqLiteDatabase.update(TABLE_GEOFENCE, values, KEY_ID + " = ?",
                new String[] { String.valueOf(geoFence.getId()) });
    }


    // Deleting single GeoFence
    public void deleteGeoFence(GeoFence geoFence) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GEOFENCE, KEY_ID + " = ?",
                new String[]{String.valueOf(geoFence.getId())});
        db.close();
    }

    // Getting GeoFence Count
    public int getGeoFenceCount() {
        String countQuery = "SELECT  * FROM " + TABLE_GEOFENCE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }
}
