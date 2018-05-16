package com.LearningTracker.LearningTrackerApp.database_management;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableRelationQuestionObjective {
    static public void createTableRelationQuestionObjectives() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS question_objective_relation " +
                    "(ID_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_GLOBAL      INT     NOT NULL, " +
                    " OBJECTIVE      TEXT     NOT NULL) ";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * method for inserting new objective into table learningObjectives
     * @param
     * @throws Exception
     */
    static public void addQuestionObjectiverRelation(String objective, String id_global) throws Exception {
        if (objective.contentEquals("")) objective = " ";
        try {
            String sql = 	"INSERT INTO question_objective_relation (ID_GLOBAL, OBJECTIVE) " +
                    "VALUES ('" + id_global + "','" + objective.replace("'", "''") + "');";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
