package com.LearningTracker.LearningTrackerApp.database_management;

import android.database.Cursor;

import com.LearningTracker.LearningTrackerApp.LTApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableIndividualQuestionForResult {
    static private String tableName = "individual_question_for_result";
    static public void createTableIndividualQuestionForResult() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " " +
                    "(ID_DIRECT_EVAL        INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL             INT    NOT NULL, " +
                    " DATE                  TEXT    NOT NULL, " +
                    " ANSWERS               TEXT    NOT NULL, " +
                    " TIME_FOR_SOLVING      INT    NOT NULL, " +
                    " QUESTION_WEIGHT       REAL    NOT NULL, " +
                    " EVAL_TYPE             TEXT    NOT NULL, " +
                    " QUANTITATIVE_EVAL     TEXT    NOT NULL, " +
                    " QUALITATIVE_EVAL       TEXT    NOT NULL, " +
                    " TEST_BELONGING        TEXT    NOT NULL, " +
                    " WEIGHTS_OF_ANSWERS    TEXT    NOT NULL) ";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public double addIndividualQuestionForStudentResult(String id_global, String quantitative_eval) {
        double quantitative_evaluation = -1;
        try {
            String sql = 	"INSERT INTO individual_question_for_result (ID_GLOBAL,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                    "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                    "VALUES ('" + id_global + "',date('now'),'none','none','none','none','" + quantitative_eval + "','none','none','none');";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        //for test: update the active questions list
        if (LTApplication.currentTestActivitySingleton != null) {
            LTApplication.currentTestActivitySingleton.getmTest().addResultAndRefreshActiveIDs(id_global, quantitative_eval);
        }

        return quantitative_evaluation;
    }

    static public void setEvalForQuestionAndStudentIDs (Double eval, String idQuestion) {
        String sql = "UPDATE individual_question_for_result SET QUANTITATIVE_EVAL = '" + eval + "' " +
                "WHERE ID_DIRECT_EVAL=(SELECT MAX (ID_DIRECT_EVAL) " +
                "FROM (SELECT * FROM 'individual_question_for_result') WHERE ID_GLOBAL='" + idQuestion + "');";

        try {
            DbHelper.dbase.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Vector<Vector<String>> getAllResults() {
        Vector<Vector<String>> results = new Vector<>();

        Cursor cursor = DbHelper.dbase.rawQuery("SELECT * FROM " + tableName, null);
        while (cursor.moveToNext()) {
            results.add( new Vector<String>());
            results.get(results.size() - 1).add(cursor.getString(1)); //id
            results.get(results.size() - 1).add(cursor.getString(3)); //answers
            results.get(results.size() - 1).add(cursor.getString(2)); //date
            results.get(results.size() - 1).add(cursor.getString(7)); //quantitative evaluation
        }

        return results;
    }
}
