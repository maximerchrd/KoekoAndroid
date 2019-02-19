package com.wideworld.koeko.database_management;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.QuestionsManagement.Result;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableIndividualQuestionForResult {
    static private String tableName = "individual_question_for_result";
    static public final int type1QuestionMultipleChoice = 0;
    static public final int type1QuestionShortAnswer = 1;
    static public final int type1Objective = 2;
    static public final int type1Test = 3;
    static public final int type2ClassroomActivity = 0;
    static public final int type2HomeworkNotSynced = 1;
    static public final int type2HomeworkSynced = 2;
    static public final int type2FreePractice = 3;
    static public void createTableIndividualQuestionForResult() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " " +
                    "(ID_DIRECT_EVAL        INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL             INT    NOT NULL, " +
                    " TYPE1                  INT, " +       //0: Question Multiple Choice; 1: Question Short Answer; 2: ObjectiveTransferable, 3: test
                    " TYPE2                  INT, " +       //0: Classroom activity; 1: homework not synced; 2: homework synced; 3: free practice
                    " DATE                  TEXT    NOT NULL, " +
                    " ANSWERS               TEXT    NOT NULL, " +
                    " TIME_FOR_SOLVING      INT    NOT NULL, " +
                    " QUESTION_WEIGHT       REAL    NOT NULL, " +
                    " EVAL_TYPE             TEXT    NOT NULL, " +       //FORMATIVE, CERTIFICATIVE
                    " QUANTITATIVE_EVAL     TEXT    NOT NULL, " +
                    " QUALITATIVE_EVAL       TEXT    NOT NULL, " +
                    " TEST_BELONGING        TEXT    NOT NULL, " +
                    " WEIGHTS_OF_ANSWERS    TEXT    NOT NULL) ";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public Boolean addIndividualTestForStudentResult(String testId, String testName, String timeForSolving, String evalType, Double quantitativeEval, String medal) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID_GLOBAL", testId);
        contentValues.put("TYPE1", 3);
        contentValues.put("DATE", timeStamp);
        contentValues.put("ANSWERS", "none");
        contentValues.put("TIME_FOR_SOLVING", timeForSolving);
        contentValues.put("QUESTION_WEIGHT", "none");
        contentValues.put("EVAL_TYPE", evalType);
        contentValues.put("QUANTITATIVE_EVAL", quantitativeEval);
        contentValues.put("QUALITATIVE_EVAL", medal);
        contentValues.put("TEST_BELONGING", testName);
        contentValues.put("WEIGHTS_OF_ANSWERS", "none");

        if (DbHelper.dbHelperSingleton.getDatabase().insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    static public Boolean addIndivResForStud(String id, String name, int type1, int type2, String answers,
                                             String timeForSolving, String evalType, Double quantitativeEval,
                                             String medal, double questionWeight, String answersWeight) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID_GLOBAL", id);
        contentValues.put("TYPE1", type1);
        contentValues.put("TYPE2", type2);
        contentValues.put("DATE", timeStamp);
        contentValues.put("ANSWERS", answers);
        contentValues.put("TIME_FOR_SOLVING", timeForSolving);
        contentValues.put("QUESTION_WEIGHT", questionWeight);
        contentValues.put("EVAL_TYPE", evalType);
        contentValues.put("QUANTITATIVE_EVAL", quantitativeEval);
        contentValues.put("QUALITATIVE_EVAL", medal);
        contentValues.put("TEST_BELONGING", name);
        contentValues.put("WEIGHTS_OF_ANSWERS", answersWeight);

        if (DbHelper.dbHelperSingleton.getDatabase().insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE) == -1 ) {
            return false;
        } else {
            return true;
        }
    }

    static public void addIndivResForStud(String id, String testName, int type1, int type2, String answers,
                                              Double quantitativeEval) {
        if (testName == null) {
            testName = "";
        }
        addIndivResForStud(id, testName, type1, type2, answers, "none", "none",
                quantitativeEval, "none" , -1, "none");
    }

    static public double addIndividualQuestionForStudentResult(String id_global, String quantitative_eval, String answer) {
        double quantitative_evaluation;
        try {
            quantitative_evaluation = Double.parseDouble(quantitative_eval);
        } catch (NumberFormatException e) {
            quantitative_evaluation = -1.0;
        }
        try {
            String sql = 	"INSERT INTO individual_question_for_result (ID_GLOBAL,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                    "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                    "VALUES (?,date('now'),?,'none','none','none',?,'none','none','none');";
            String[] sqlArgs = new String[]{
                    id_global,
                    answer,
                    quantitative_eval
            };
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql, sqlArgs);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }

        //for test: update the active questions list and the answered questions list
        if (Koeko.currentTestActivitySingleton != null) {
            Koeko.currentTestActivitySingleton.getmTest().addResultAndRefreshActiveIDs(id_global, quantitative_eval);
            Koeko.currentTestActivitySingleton.getmTest().getAnsweredQuestionIds().put(id_global, Double.valueOf(quantitative_eval));
            Koeko.currentTestActivitySingleton.testIsFinished = Koeko.currentTestActivitySingleton.checkIfTestFinished();

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = () -> Koeko.currentTestActivitySingleton.finalizeTest();
            mainHandler.post(myRunnable);
        }

        return quantitative_evaluation;
    }

    static public double addIndividualQuestionForStudentResult(String id_global, String quantitative_eval, Integer type, String test) {
        double quantitative_evaluation = -1;
        try {
            String sql = 	"INSERT INTO individual_question_for_result (ID_GLOBAL,TYPE1,DATE,ANSWERS,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                    "QUANTITATIVE_EVAL,QUALITATIVE_EVAL,TEST_BELONGING,WEIGHTS_OF_ANSWERS) " +
                    "VALUES ('" + id_global  + "', '" + type + "',date('now'),'none','none','none','none','" + quantitative_eval + "','none','" +
                     test + "','none');";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        //for test: update the active questions list
        if (Koeko.currentTestActivitySingleton != null) {
            Koeko.currentTestActivitySingleton.getmTest().addResultAndRefreshActiveIDs(id_global, quantitative_eval);
        }

        return quantitative_evaluation;
    }

    static public void setEvalForQuestion(Double eval, String idQuestion) {
        String sql = "UPDATE individual_question_for_result SET QUANTITATIVE_EVAL = '" + eval + "' " +
                "WHERE ID_DIRECT_EVAL=(SELECT MAX (ID_DIRECT_EVAL) " +
                "FROM (SELECT * FROM 'individual_question_for_result') WHERE ID_GLOBAL='" + idQuestion + "');";

        try {
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Vector<Vector<String>> getAllResults() {
        Vector<Vector<String>> results = new Vector<>();

        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery("SELECT ID_GLOBAL,ANSWERS,DATE,QUANTITATIVE_EVAL,TYPE1," +
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

    static public ArrayList<Result> getUnsyncedHomeworks() {
        ArrayList<Result> results = new ArrayList<>();

        String sql = "SELECT ID_GLOBAL,ANSWERS,DATE,QUANTITATIVE_EVAL,TYPE1," +
                "QUALITATIVE_EVAL,TEST_BELONGING,TIME_FOR_SOLVING,QUESTION_WEIGHT,EVAL_TYPE," +
                "WEIGHTS_OF_ANSWERS FROM " + tableName + " WHERE TYPE2 = ?";
        String[] args = new String[]{String.valueOf(type2HomeworkNotSynced)};
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(sql, args);
        while (cursor.moveToNext()) {
            Result result = new Result();
            result.setResourceUid(cursor.getString(0));
            result.setAnswers(cursor.getString(1));
            result.setDate(cursor.getString(2));
            result.setQuantitativeEval(cursor.getString(3));
            result.setType1(cursor.getInt(4));
            result.setQualitativeEval(cursor.getString(5));
            result.setTestBelonging(cursor.getString(6));
            result.setTimeForSolving(cursor.getInt(7));
            result.setResourceWeight(cursor.getDouble(8));
            result.setEvalType(cursor.getString(9));
            result.setAnswersWeights(cursor.getString(10));
            result.setType2(DbTableIndividualQuestionForResult.type2HomeworkSynced);
            results.add(result);
        }

        return results;
    }

    public static void setAllHomeworkSynced() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("TYPE2", type2HomeworkSynced);
        DbHelper.dbHelperSingleton.getDatabase().update(tableName, contentValues, "TYPE2 = ?",
                new String[]{String.valueOf(type2HomeworkNotSynced)});
    }
}
