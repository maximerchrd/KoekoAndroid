package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.UUID;

public class DbTableSettings {
    private static final String TABLE_SETTING = "setting";
    // tasks Table Columns names for the settings table
    private static final String KEY_IDsettings = "idsettings";
    private static final String SETTING_KEY = "SETTING_KEY";
    private static final String SETTING_VALUE = "SETTING_VALUE";
    private static final String KEY_NAME = "name";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_MASTER = "master";
    private static final String KEY_AUTOMATIC_CONNECTION = "automatic_connection";
    private static final String KEY_INTERNET_SERVER = "internet_server";
    private static final String KEY_HOTSPOT_AVAILABLE = "hotspot_available";
    private static String uuid = "";

    private static String defaultMaster = "192.168.1.100";
    private static String defaultInternetServer = "192.168.1.100";

    private static String TAG = "DbTableSettings";

    static public void createTable(String noName) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_SETTING + " ( "
                + KEY_IDsettings + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SETTING_KEY +" TEXT,"
                + SETTING_VALUE + " TEXT)";
        DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);

        String uuid = DbTableSettings.getUUID();
        if (uuid == null || uuid.contentEquals("")) {
            insertSetting(KEY_NAME, noName);
            insertSetting(KEY_MASTER, defaultMaster);
            insertSetting(KEY_UUID, UUID.randomUUID().toString());
            insertSetting(KEY_AUTOMATIC_CONNECTION, "1");
            insertSetting(KEY_HOTSPOT_AVAILABLE, "0");
            insertSetting(KEY_INTERNET_SERVER, defaultInternetServer);
        }
    }

    static public void addName(String newname)
    {
        updateSetting(KEY_NAME, newname);
    }

    static public String getName() {
        return getSetting(KEY_NAME);
    }

    static public void addMaster(String newMaster)
    {
        updateSetting(KEY_MASTER, newMaster);
    }
    //get name from db
    static public String getMaster() {
        return getSetting(KEY_MASTER);
    }

    static public String getUUID() {
        if (DbTableSettings.uuid.length() == 0) {
            return getSetting(KEY_UUID);
        } else {
            return DbTableSettings.uuid;
        }
    }

    static public void setAutomaticConnection(Integer automaticConnection) {
        updateSetting(KEY_AUTOMATIC_CONNECTION, automaticConnection.toString());
    }

    static public Integer getAutomaticConnection() {
        Integer automaticConnection;
        String automatiConnectionString = getSetting(KEY_AUTOMATIC_CONNECTION);
        try {
            automaticConnection = Integer.valueOf(automatiConnectionString);
        } catch (NumberFormatException e) {
            automaticConnection = 1;
            e.printStackTrace();
        }
        return automaticConnection;
    }

    static public void setHotspotAvailable(Integer hotspotAvailable) {
        updateSetting(KEY_HOTSPOT_AVAILABLE, hotspotAvailable.toString());
    }

    static public Integer getHotspotAvailable() {
        Integer hotspotAvailable;
        String hotspotAvailableString = getSetting(KEY_HOTSPOT_AVAILABLE);

        try {
            hotspotAvailable = Integer.valueOf(hotspotAvailableString);
        } catch (NumberFormatException e) {
            hotspotAvailable = 0;
            e.printStackTrace();
        }

        return hotspotAvailable;
    }

    static public void insertInternetServer(String internetServer) {
        updateSetting(KEY_INTERNET_SERVER, internetServer);
    }

    static public String getInternetServer() {
        return getSetting(KEY_INTERNET_SERVER);
    }

    static private void insertSetting(String settingKey, String settingValue) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(SETTING_KEY, settingKey);
        insertValues.put(SETTING_VALUE, settingValue);
        DbHelper.dbHelperSingleton.getDatabase().insert(TABLE_SETTING, null, insertValues);
    }

    static private void updateSetting(String settingKey, String settingValue) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(SETTING_KEY, settingKey);
        insertValues.put(SETTING_VALUE, settingValue);
        String[] args = new String[]{settingKey};
        DbHelper.dbHelperSingleton.getDatabase().update(TABLE_SETTING, insertValues, SETTING_KEY + " = ?", args);
    }

    static private String getSetting(String settingKey) {
        String settingValue = "";
        String selectQuery = "SELECT " + SETTING_VALUE + " FROM " + TABLE_SETTING + " WHERE " + SETTING_KEY + " = ?";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, new String[]{settingKey});
        if (cursor.moveToFirst()) {
            settingValue = cursor.getString(0);
        }

        return settingValue;
    }
}
