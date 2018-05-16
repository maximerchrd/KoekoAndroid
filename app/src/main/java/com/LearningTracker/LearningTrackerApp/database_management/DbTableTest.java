package com.LearningTracker.LearningTrackerApp.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.Test;

import java.util.Vector;

/**
 * Created by maximerichard on 15.05.18.
 */
public class DbTableTest {
    static private String tableName = "test";
    static private String key_idGlobal = "ID_TEST_GLOBAL";
    static private String key_testName = "TEST_NAME";
    static private String key_questions_ids = "QUESTION_IDS";

    static public void createTableTest() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                "(ID      INTEGER PRIMARY KEY AUTOINCREMENT," +
                key_idGlobal + " TEXT     NOT NULL, " +
                key_testName + " TEXT     NOT NULL, " +
                key_questions_ids + " TEXT, " +
                " UNIQUE (" + key_idGlobal + ") )";
        DbHelper.dbase.execSQL(sql);
    }

    static public Boolean insertTest(Test test) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(key_idGlobal, String.valueOf(test.getIdGlobal()));
        contentValues.put(key_testName, test.getTestName());
        contentValues.put(key_questions_ids, test.serializeQuestionIDs());

        if (DbHelper.dbase.insert(tableName, null, contentValues) == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    static public String getNameFromTestID(Long testID) {
        String testName = "";
        Cursor cursor = DbHelper.dbase.rawQuery("SELECT " + key_testName + " FROM " + tableName + " WHERE " +
                key_idGlobal + " = ?", new String[]{String.valueOf(testID)});
        while (cursor.moveToNext()) {
            testName = cursor.getString(0);
        }
        return testName;
    }

    static public Vector<String> getQuestionIDsFromTestName(String testName) {
        Vector<String> questionIds = new Vector<>();
        String unparsedIDs = "";
        Cursor cursor = DbHelper.dbase.rawQuery("SELECT " + key_questions_ids + " FROM " + tableName + " WHERE " +
                key_testName + " = ?", new String[]{testName});
        while (cursor.moveToNext()) {
            unparsedIDs = cursor.getString(0);
        }

        String[] idsArray = unparsedIDs.split("|");
        for (String id: idsArray) {
            questionIds.add(id);
        }

        return questionIds;
    }

    static public Vector<String[]> getAllTests() {
        Vector<String[]> testNamesAndIds = new Vector<>();

        Cursor cursor = DbHelper.dbase.rawQuery("SELECT " + key_testName + "," + key_idGlobal +
                " FROM " + tableName, null);
        while (cursor.moveToNext()) {
            testNamesAndIds.add(new String[] {cursor.getString(0), cursor.getString(1)});
        }

        return testNamesAndIds;
    }

    static public Vector<String> getObjectivesFromTestID(Long testId) {
        Vector<String> objectives = new Vector<>();

        String tableObjective = DbTableRelationTestObjective.getTableName();
        String tableRelation = DbTableRelationTestObjective.getTableName();

        Cursor cursor = DbHelper.dbase.rawQuery("SELECT " + DbTableLearningObjective.getKey_objective() +
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
