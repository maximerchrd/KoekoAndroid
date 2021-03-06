package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Vector;

/**
 * Created by maximerichard on 15.05.18.
 */
public class DbTableRelationQuestionQuestion {

    static private String tableName = "question_question_relation";
    static private String key_idGlobal1 = "ID_GLOBAL_1";
    static private String key_idGlobal2 = "ID_GLOBAL_2";
    static private String key_testName = "TEST_NAME";
    static private String key_condition = "CONDITION";

    public static String getTableName() {
        return tableName;
    }

    public static String getKey_idGlobal1() {
        return key_idGlobal1;
    }

    public static String getKey_idGlobal2() {
        return key_idGlobal2;
    }

    public static String getKey_testName() {
        return key_testName;
    }

    static public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                "(ID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                key_idGlobal1 + " TEXT     NOT NULL, " +
                key_idGlobal2 + " TEXT     NOT NULL, " +
                key_testName + " TEXT     NOT NULL, " +
                key_condition + " TEXT, " +
                "CONSTRAINT unq UNIQUE (" + key_idGlobal1 +", " + key_idGlobal2 +", " + key_testName +")) ";
        DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
    }

    static public Boolean insertRelationQuestionQuestion(Long idGlobal1, Long idGlobal2, String testName, String condition) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(key_idGlobal1, String.valueOf(idGlobal1));
        contentValues.put(key_idGlobal2, String.valueOf(idGlobal2));
        contentValues.put(key_testName, String.valueOf(testName));
        contentValues.put(key_condition, condition);

        if (DbHelper.dbHelperSingleton.getDatabase().insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    static public Vector<String[]> getTestMapForTest(String testName) {
        Vector<String[]> testMap = new Vector<>();

        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT * FROM " + tableName + " WHERE " +
                key_testName + " = ?", new String[]{String.valueOf(testName)});
        while (cursor.moveToNext()) {
            testMap.add(new String[] {cursor.getString(1),
                cursor.getString(2), cursor.getString(3), cursor.getString(4)});
        }

        return testMap;
    }
}