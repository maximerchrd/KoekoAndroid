package com.LearningTracker.LearningTrackerApp.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.LearningTracker.LearningTrackerApp.LTApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
                    " TYPE                  INT, " +       //0: Question Multiple Choice; 1: Question Short Answer; 2: Objective, 3: test
                    " DATE                  TEXT    NOT NULL, " +
                    " ANSWERS               TEXT    NOT NULL, " +
                    " TIME_FOR_SOLVING      INT    NOT NULL, " +
                    " QUESTION_WEIGHT       REAL    NOT NULL, " +
                    " EVAL_TYPE             TEXT    NOT NULL, " +       //FORMATIVE, CERTIFICATIVE
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

    static public Boolean addIndividualTestForStudentResult(String testId, String testName, String timeForSolving, String evalType, Double quantitativeEval, String medal) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID_GLOBAL", testId);
        contentValues.put("TYPE", 3);
        contentValues.put("DATE", timeStamp);
        contentValues.put("ANSWERS", "none");
        contentValues.put("TIME_FOR_SOLVING", timeForSolving);
        contentValues.put("QUESTION_WEIGHT", "none");
        contentValues.put("EVAL_TYPE", evalType);
        contentValues.put("QUANTITATIVE_EVAL", quantitativeEval);
        contentValues.put("QUALITATIVE_EVAL", medal);
        contentValues.put("TEST_BELONGING", testName);
        contentValues.put("WEIGHTS_OF_ANSWERS", "none");

        if (DbHelper.dbase.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    static public double addIndividualQuestionForStudentResult(String id_global, String quantitative_eval, String answer) {
        double quantitative_evaluation = -1;
        try {
            String sql = 	"INSERT INTO individual_question_for_result (ID_GLOBAL,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                    "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                    "VALUES (?,date('now'),?,'none','none','none',?,'none','none','none');";
            String[] sqlArgs = new String[]{
                    id_global,
                    answer,
                    quantitative_eval
            };
            DbHelper.dbase.execSQL(sql, sqlArgs);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        //for test: update the active questions list and the answered questions list
        if (LTApplication.currentTestActivitySingleton != null) {
            LTApplication.currentTestActivitySingleton.getmTest().addResultAndRefreshActiveIDs(id_global, quantitative_eval);
            LTApplication.currentTestActivitySingleton.getmTest().getAnsweredQuestionIds().add(id_global);
            LTApplication.currentTestActivitySingleton.getmTest().getQuestionsScores().add(Double.valueOf(quantitative_eval));
            Handler mainHandler = new Handler(Looper.getMainLooper());

            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    LTApplication.currentTestActivitySingleton.checkIfTestFinished();
                }
            };
            mainHandler.post(myRunnable);
        }

        return quantitative_evaluation;
    }

    static public double addIndividualQuestionForStudentResult(String id_global, String quantitative_eval, Integer type, String test) {
        double quantitative_evaluation = -1;
        try {
            String sql = 	"INSERT INTO individual_question_for_result (ID_GLOBAL,TYPE,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                    "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                    "VALUES ('" + id_global  + "', '" + type + "',date('now'),'none','none','none','none','" + quantitative_eval + "','none','" +
                     test + "','none');";
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

    static public void setEvalForQuestion(Double eval, String idQuestion) {
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

        Cursor cursor = DbHelper.dbase.rawQuery("SELECT ID_GLOBAL,ANSWERS,DATE,QUANTITATIVE_EVAL,TYPE," +
                "QUALITATIVE_EVAL,TEST_BELONGING FROM " + tableName, null);
        while (cursor.moveToNext()) {
            results.add( new Vector<String>());
            results.get(results.size() - 1).add(cursor.getString(0)); //id
            results.get(results.size() - 1).add(cursor.getString(1)); //answers
            results.get(results.size() - 1).add(cursor.getString(2)); //date
            results.get(results.size() - 1).add(cursor.getString(3)); //quantitative evaluation
            results.get(results.size() - 1).add(cursor.getString(4)); //type
            results.get(results.size() - 1).add(cursor.getString(5)); //qualitative evaluation
            results.get(results.size() - 1).add(cursor.getString(6)); //test belonging
        }

        return results;
    }
}
