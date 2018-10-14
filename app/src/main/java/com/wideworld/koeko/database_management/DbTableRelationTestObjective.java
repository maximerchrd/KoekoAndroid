package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by maximerichard on 15.05.18.
 */
public class DbTableRelationTestObjective {

    static private String tableName = "test_objective_relation";
    static private String key_idTest = "ID_TEST";
    static private String key_idObjective = "ID_OBJECTIVE";

    public static String getTableName() {
        return tableName;
    }

    public static String getKey_idTest() {
        return key_idTest;
    }

    public static String getKey_idObjective() {
        return key_idObjective;
    }

    static public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                "(ID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                key_idTest + " TEXT     NOT NULL, " +
                key_idObjective + " TEXT     NOT NULL, " +
                "CONSTRAINT unq UNIQUE (" + key_idTest +", " + key_idObjective +")) ";
        DbHelper.dbase.execSQL(sql);
    }

    static public Boolean insertRelationTestObjective(String idTest, String idObjective) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key_idTest, idTest);
        contentValues.put(key_idObjective, idObjective);

        if (DbHelper.dbase.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1 ) {
            return false;
        } else {
            return true;
        }
    }
}
