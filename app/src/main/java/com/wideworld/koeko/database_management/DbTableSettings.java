package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.wideworld.koeko.R;

import java.util.UUID;

public class DbTableSettings {
    private static final String TABLE_SETTINGS = "settings";
    // tasks Table Columns names for the settings table
    private static final String KEY_IDsettings = "idsettings";
    private static final String KEY_NAME = "name";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_MASTER = "master";
    private static final String KEY_AUTOMATIC_CONNECTION = "automatic_connection";

    private static String TAG = "DbTableSettings";

    static public void createTable(String noName) {
        String uuid = DbTableSettings.getUUID();
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " ( "
                + KEY_IDsettings + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_NAME +" TEXT,"
                + KEY_MASTER + " TEXT,"
                + KEY_UUID + " TEXT,"
                + KEY_AUTOMATIC_CONNECTION +" INTEGER)";
        DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        if (uuid != null && uuid.contentEquals("")) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, noName);
            values.put(KEY_MASTER, "192.168.1.100");
            values.put(KEY_UUID, UUID.randomUUID().toString());
            values.put(KEY_AUTOMATIC_CONNECTION, 1);
            // Inserting of Replacing Row
            DbHelper.dbHelperSingleton.getDatabase().insert(TABLE_SETTINGS, null, values);
        }
    }

    static public void addName(String newname)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, newname);
        // Replacing Row
        DbHelper.dbHelperSingleton.getDatabase().update(TABLE_SETTINGS, values, null, null);
    }

    static public String getName() {
        // Select All Query
        String name = "";
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToPosition(0)) {
            name = cursor.getString(1);
        }
        // return string name
        return name;
    }
    //add new name
    static public void addMaster(String newname)
    {
        //SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MASTER, newname);
        // Replacing Row
        DbHelper.dbHelperSingleton.getDatabase().update(TABLE_SETTINGS, values, null, null);
    }
    //get name from db
    static public String getMaster() {
        // Select All Query
        String master = "";
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToPosition(0)) {
            master = cursor.getString(2);
        }
        // return string name
        return master;
    }

    static public String getUUID() {
        // Select All Query
        String name = "";
        String selectQuery = "SELECT " + KEY_UUID + " FROM " + TABLE_SETTINGS;
        try {
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
            if (cursor.moveToPosition(0)) {
                name = cursor.getString(0);
            }
        } catch (SQLiteException sqe) {
            if (sqe.toString().contains("no such table")) {
                Log.v(TAG, "no table settings yet");
            } else {
                sqe.printStackTrace();
            }
        }
        // return string name
        return name;
    }

    static public void setAutomaticConnection(Integer automaticConnection) {
        //SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_AUTOMATIC_CONNECTION, automaticConnection);
        // Replacing Row
        DbHelper.dbHelperSingleton.getDatabase().update(TABLE_SETTINGS, values, null, null);
    }

    static public Integer getAutomaticConnection() {
        // Select All Query
        Integer automaticCorrection = 1;
        String selectQuery = "SELECT " + KEY_AUTOMATIC_CONNECTION + " FROM " + TABLE_SETTINGS;
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
        if (cursor.moveToPosition(0)) {
            automaticCorrection = cursor.getInt(0);
        }
        // return string name
        return automaticCorrection;
    }
}
