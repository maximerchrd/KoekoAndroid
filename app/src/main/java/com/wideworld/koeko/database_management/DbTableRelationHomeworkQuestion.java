package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wideworld.koeko.QuestionsManagement.Homework;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DbTableRelationHomeworkQuestion {
    static private String KEY_TABLE_HOMEWORK_QUESTION = "homework_question_relation";
    static private String KEY_HOMEWORK_NAME = "homework_name";
    static private String KEY_QUESTION_ID = "question_id";

    static public void createTableHomeworkQuestionRelation() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_HOMEWORK_QUESTION +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_HOMEWORK_NAME + " TEXT  NOT NULL, " +
                KEY_QUESTION_ID + " TEXT, " +
                "CONSTRAINT unq UNIQUE (" + KEY_HOMEWORK_NAME + ", " + KEY_QUESTION_ID + "))";
        DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
    }

    static public ArrayList<String> getQuestionIdsFromHomeworkName(String homeworkName) {
        ArrayList<String> studentIds = new ArrayList<>();
        String selectQuery = "SELECT " + KEY_QUESTION_ID + " FROM " + KEY_TABLE_HOMEWORK_QUESTION + " WHERE " + KEY_HOMEWORK_NAME + " = ?";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, new String[]{homeworkName});
        while (cursor.moveToNext()) {
            studentIds.add(cursor.getString(0));
        }
        return studentIds;
    }

    static public void insertHomeworkQuestionRelation(String homeworkName, String questionId) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_HOMEWORK_NAME, homeworkName);
        insertValues.put(KEY_QUESTION_ID, questionId);
        DbHelper.dbHelperSingleton.getDatabase().insertWithOnConflict(KEY_TABLE_HOMEWORK_QUESTION, null, insertValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    static public void deleteAllRelationsForHomework(String homeworkName) {
        DbHelper.dbHelperSingleton.getDatabase().delete(KEY_TABLE_HOMEWORK_QUESTION, KEY_HOMEWORK_NAME + " = ?", new String[]{homeworkName});
    }

    static public void updateHomeworkName(String newname, String oldname) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_HOMEWORK_NAME, newname);

        DbHelper.dbHelperSingleton.getDatabase().update(KEY_TABLE_HOMEWORK_QUESTION, insertValues,
                KEY_HOMEWORK_NAME + "=?", new String[]{oldname});
    }
}
