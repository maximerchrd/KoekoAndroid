package com.LearningTracker.LearningTrackerApp.database_management;

/**
 * Created by maximerichard on 21.02.18.
 */
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbTableAnswerOptions {
    static public void createTableAnswerOptions() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS answer_options " +
                    "(ID_ANSWEROPTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_ANSWEROPTION_GLOBAL      INT     NOT NULL, " +
                    " OPTION           TEXT    NOT NULL, " +
                    "UNIQUE ( OPTION ));";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addAnswerOption(String questionID, String option) {
        try {
            String sql;
            ContentValues initialValues = new ContentValues();
            initialValues.put("ID_ANSWEROPTION_GLOBAL", "2000000");
            initialValues.put("OPTION",option);
            int rowsAffected = (int)DbHelper.dbase.insertWithOnConflict("answer_options",null,initialValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (rowsAffected > -1) {
                sql = "UPDATE answer_options SET ID_ANSWEROPTION_GLOBAL = ID_ANSWEROPTION_GLOBAL + ID_ANSWEROPTION WHERE ID_ANSWEROPTION = (SELECT MAX(ID_ANSWEROPTION) FROM answer_options)";
                DbHelper.dbase.execSQL(sql);
            } else {
                System.out.println(option + "not added");
            }
            DbTableRelationQuestionAnserOption.addRelationQuestionAnserOption(questionID, option);
        } catch ( SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void removeOptionsRelationsQuestion(String questionID) throws Exception {
        Connection c = null;
        Statement stmt = null;
        stmt = null;
        try {
            String sql = 	"DELETE FROM question_answeroption_relation WHERE ID_GLOBAL='" + questionID + "';";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}