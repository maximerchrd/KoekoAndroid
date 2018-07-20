package com.LearningTracker.LearningTrackerApp.database_management;

import android.database.Cursor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

/**
 * Created by maximerichard on 20.02.18.
 */
public class DbTableRelationQuestionSubject {
    static public void createTableSubject() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS question_subject_relation " +
                    "(ID_SUBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_SUBJECT_GLOBAL      INT     NOT NULL, " +
                    " SUBJECT_LEVEL      INT NOT NULL) ";
            DbHelper.dbase.execSQL(sql);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and a subject
     * by linking the last added question with the subject given as parameter
     *
     * @param subject,questionID
     * @throws Exception
     */
    static public void addRelationQuestionSubject(String questionID, String subject) {
        try {
            String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                    "WHERE ID_GLOBAL='" + questionID + "' " +
                    "AND ID_SUBJECT_GLOBAL= (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = '" + subject + "');";
            Cursor cursor = DbHelper.dbase.rawQuery(query, null);
            Vector<String> queries = new Vector<>();
            while (cursor.moveToNext()) {
                queries.add(cursor.getString(0));
            }
            cursor.close();
            if (queries.size() == 0) {
                String sql = "INSERT INTO question_subject_relation (ID_GLOBAL, ID_SUBJECT_GLOBAL, SUBJECT_LEVEL) SELECT '" + questionID + "',t2.ID_SUBJECT_GLOBAL," +
                        "'1' FROM subjects t2 WHERE t2.SUBJECT='" + subject + "';";
                DbHelper.dbase.execSQL(sql);
            }

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    static public Vector<String> getQuestionsIdsForSubject(String subject) {
        Vector<String> questionIDs = new Vector<>();
        try {
            String query = "SELECT ID_GLOBAL FROM question_subject_relation " +
                    "WHERE ID_SUBJECT_GLOBAL = (SELECT ID_SUBJECT_GLOBAL FROM subjects WHERE SUBJECT = '" + subject + "');";
            Cursor cursor = DbHelper.dbase.rawQuery(query, null);
            while (cursor.moveToNext()) {
                questionIDs.add(cursor.getString(0));
            }
            cursor.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return questionIDs;
    }
}