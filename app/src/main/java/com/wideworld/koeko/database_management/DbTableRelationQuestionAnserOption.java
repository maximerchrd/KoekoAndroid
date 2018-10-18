package com.wideworld.koeko.database_management;

import android.database.SQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by maximerichard on 21.02.18.
 */
public class DbTableRelationQuestionAnserOption {
    private static String key_idGlobal = "ID_GLOBAL";
    private static String key_idAnswerOption = "ID_ANSWEROPTION_GLOBAL";
    static public void createTableSubject() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS question_answeroption_relation " +
                    "(ID_QUEST_ANSOPTION_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_ANSWEROPTION_GLOBAL      INT     NOT NULL, " +
                    "CONSTRAINT unq UNIQUE (" + key_idGlobal +", " + key_idAnswerOption +")) ";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and an answer option
     * @param questionID, option
     * @throws SQLException
     */
    static public void addRelationQuestionAnserOption(String questionID, String option) {
        try {
            String sql = "INSERT OR REPLACE INTO question_answeroption_relation (ID_GLOBAL, ID_ANSWEROPTION_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_ANSWEROPTION_GLOBAL FROM short_answer_questions t1, answer_options t2 " +
                    "WHERE t1.ID_GLOBAL = '"+ questionID + "' " +
                    "AND t2.OPTION='" + option + "';";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
}
