package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wideworld.koeko.QuestionsManagement.Homework;
import java.util.ArrayList;

public class DbTableHomework {
    static private String KEY_TABLE_HOMEWORK = "homework";
    static private String KEY_UID = "HW_UID";
    static private String KEY_NAME = "name";
    static private String KEY_IDCODE= "id_code";
    static private String KEY_DUEDATE = "due_date";

    static public void createTableHomeworks() {
        String sql = "CREATE TABLE IF NOT EXISTS " + KEY_TABLE_HOMEWORK +
                " (ID       INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_UID + " TEXT, " +
                KEY_NAME + " TEXT  NOT NULL, " +
                KEY_IDCODE + " TEXT, " +
                KEY_DUEDATE + " TEXT," +
                " UNIQUE (" + KEY_UID + "))";
        DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
    }

    static public void insertHomework(Homework homework) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_UID, homework.getUid());
        insertValues.put(KEY_NAME, homework.getName());
        insertValues.put(KEY_IDCODE, homework.getIdCode());
        insertValues.put(KEY_DUEDATE, homework.getDueDate());
        DbHelper.dbHelperSingleton.getDatabase().insertWithOnConflict(KEY_TABLE_HOMEWORK, null, insertValues, SQLiteDatabase.CONFLICT_REPLACE);

        for (String questionId : homework.getQuestions()) {
            DbTableRelationHomeworkQuestion.insertHomeworkQuestionRelation(homework.getName(), questionId);
        }
    }

    static public void updateHomework(Homework homework, String oldName) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(KEY_NAME, homework.getName());
        insertValues.put(KEY_IDCODE, homework.getIdCode());
        insertValues.put(KEY_DUEDATE, homework.getDueDate());
        String[] args = new String[]{oldName};
        DbHelper.dbHelperSingleton.getDatabase().update(KEY_TABLE_HOMEWORK, insertValues, KEY_NAME + " = ?", args);
    }

    static public ArrayList<Homework> getHomeworksWithCode(String code) {
        ArrayList<Homework> homeworks = new ArrayList<>();
        String selectQuery = "SELECT " + KEY_NAME + ", " + KEY_UID + ", " + KEY_DUEDATE + " FROM " + KEY_TABLE_HOMEWORK + " WHERE " + KEY_IDCODE + " = ?";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, new String[]{code});
        while (cursor.moveToNext()) {
            Homework homework = new Homework();
            homework.setName(cursor.getString(0));
            homework.setUid(cursor.getString(1));
            homework.setDueDate(cursor.getString(2));
            homework.setIdCode(code);
            homeworks.add(homework);
        }

        return homeworks;
    }
}
