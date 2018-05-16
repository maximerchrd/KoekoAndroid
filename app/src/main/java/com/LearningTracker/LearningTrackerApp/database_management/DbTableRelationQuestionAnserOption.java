package com.LearningTracker.LearningTrackerApp.database_management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Created by maximerichard on 21.02.18.
 */
public class DbTableRelationQuestionAnserOption {
    static public void createTableSubject() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS question_answeroption_relation " +
                    "(ID_QUEST_ANSOPTION_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " ID_ANSWEROPTION_GLOBAL      INT     NOT NULL) ";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * Method that adds a relation between a question and an answer option
     * @param questionID, option
     * @throws Exception
     */
    static public void addRelationQuestionAnserOption(String questionID, String option) throws Exception {
        try {
            String sql = "INSERT INTO question_answeroption_relation (ID_GLOBAL, ID_ANSWEROPTION_GLOBAL) " +
                    "SELECT t1.ID_GLOBAL,t2.ID_ANSWEROPTION_GLOBAL FROM short_answer_questions t1, answer_options t2 " +
                    "WHERE t1.ID_GLOBAL = '"+ questionID + "' " +
                    "AND t2.OPTION='" + option + "';";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
