package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.wideworld.koeko.QuestionsManagement.Test;

import java.util.Vector;

/**
 * Created by maximerichard on 15.05.18.
 */
public class DbTableTest {
    static private String tableName = "test";
    static private String key_idGlobal = "ID_TEST_GLOBAL";
    static private String key_testName = "TEST_NAME";
    static private String key_questions_ids = "QUESTION_IDS";
    static private String key_test_type = "TEST_TYPE";
    static private String key_medals_instructions = "MEDALS_INSTRUCTIONS";
    static private String key_mediafile_name = "MEDIA_FILE";

    public static String getTableName() {
        return tableName;
    }

    public static String getKey_idGlobal() {
        return key_idGlobal;
    }

    public static String getKey_testName() {
        return key_testName;
    }

    public static String getKey_test_type() {
        return key_test_type;
    }

    public static String getKey_questions_ids() {
        return key_questions_ids;
    }

    static public void createTableTest() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                "(ID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                key_idGlobal + " TEXT     NOT NULL, " +
                key_testName + " TEXT     NOT NULL, " +
                key_test_type + " TEXT     NOT NULL, " +
                key_questions_ids + " TEXT, " +
                key_medals_instructions + " TEXT, " +
                key_mediafile_name + " TEXT, " +
                " UNIQUE (" + key_idGlobal + ") )";
        DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
    }

    static public Boolean insertTest(Test test) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(key_idGlobal, String.valueOf(test.getIdGlobal()));
        contentValues.put(key_testName, test.getTestName());
        contentValues.put(key_test_type, test.getTestType());
        contentValues.put(key_questions_ids, test.serializeQuestionIDs());
        contentValues.put(key_medals_instructions, test.getMedalsInstructionsString());
        contentValues.put(key_mediafile_name, test.getMediaFileName());


        if (DbHelper.dbHelperSingleton.getDatabase().insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    static public String getNameFromTestID(Long testID) {
        String testName = "";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT " + key_testName + " FROM " + tableName + " WHERE " +
                key_idGlobal + " = ?", new String[]{String.valueOf(testID)});
        while (cursor.moveToNext()) {
            testName = cursor.getString(0);
        }
        return testName;
    }

    static public String getTypeFromTestName(String testName) {
        String testType = "";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT " + key_test_type + " FROM " + tableName + " WHERE " +
                key_testName + " = ?", new String[]{String.valueOf(testName)});
        while (cursor.moveToNext()) {
            testType = cursor.getString(0);
        }
        return testType;
    }

    static public String getMediaFileFromTestName(String testName) {
        String fileName = "";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT " + key_mediafile_name + " FROM " + tableName + " WHERE " +
                key_testName + " = ?", new String[]{String.valueOf(testName)});
        while (cursor.moveToNext()) {
            fileName = cursor.getString(0);
        }
        return fileName;
    }

    static public Test getTestFromTestId(String tesId) {
        Test test = new Test();
        //Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'android.database.Cursor android.database.sqlite.SQLiteDatabase.rawQuery(java.lang.String, java.lang.String[])' on a null object reference
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT * FROM " + tableName + " WHERE " +
                key_idGlobal + " = ?", new String[]{String.valueOf(tesId)});
        if (cursor.moveToNext()) {
            test.setIdGlobal(Long.valueOf(cursor.getString(1)));
            test.setTestName(cursor.getString(2));
            test.setTestType(cursor.getString(3));
            test.setMedalsInstructionsString(cursor.getString(5));

            Vector<String> questionIds = new Vector<>();
            String[] idsArray = cursor.getString(4).split("\\|");
            for (String id: idsArray) {
                questionIds.add(id);
            }
            test.setQuestionsIDs(questionIds);
        } else {
            test = null;
        }
        return test;
    }

    static public Vector<String> getQuestionIDsFromTestName(String testName) {
        Vector<String> questionIds = new Vector<>();
        String unparsedIDs = "";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT " + key_questions_ids + " FROM " + tableName + " WHERE " +
                key_testName + " = ?", new String[]{testName});
        while (cursor.moveToNext()) {
            unparsedIDs = cursor.getString(0);
        }

        String[] idsArray = unparsedIDs.split("\\|");
        for (String id: idsArray) {
            questionIds.add(id);
        }

        return questionIds;
    }

    static public Vector<String[]> getAllTests() {
        Vector<String[]> testNamesAndIds = new Vector<>();

        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT " + key_testName + "," + key_idGlobal +
                " FROM " + tableName, null);
        while (cursor.moveToNext()) {
            testNamesAndIds.add(new String[] {cursor.getString(0), cursor.getString(1)});
        }

        return testNamesAndIds;
    }

    static public Vector<String[]> getAllTestsWithObjectives() {
        Vector<String[]> testNamesAndIds = new Vector<>();

        String sql = "SELECT " + key_testName + "," + key_idGlobal + " FROM " + tableName +
                " INNER JOIN " + DbTableRelationTestObjective.getTableName() + " ON " +
                tableName + "." + key_idGlobal + " = " + DbTableRelationTestObjective.getTableName() + "." + DbTableRelationTestObjective.getKey_idTest();
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            testNamesAndIds.add(new String[] {cursor.getString(1), cursor.getString(2)});
        }

        return testNamesAndIds;
    }

    static public Vector<String> getObjectivesFromTestID(Long testId) {
        Vector<String> objectives = new Vector<>();

        String tableObjective = DbTableRelationTestObjective.getTableName();
        String tableRelation = DbTableRelationTestObjective.getTableName();

        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT " + DbTableLearningObjective.getKey_objective() +
                " FROM " + tableObjective +
                " INNER JOIN " + tableRelation + " ON " + tableObjective + "." + DbTableLearningObjective.getKey_objectiveId() +
                " = " + tableRelation + "." + DbTableRelationTestObjective.getKey_idObjective() +
                " INNER JOIN " + tableName + " ON " + tableRelation + "." + DbTableRelationTestObjective.getKey_idTest() +
                " = " + tableName + "." + key_idGlobal +
                " WHERE " + key_idGlobal + " = " + testId, null);

        while (cursor.moveToNext()) {
            objectives.add(cursor.getString(0));
        }

        return objectives;
    }
}
