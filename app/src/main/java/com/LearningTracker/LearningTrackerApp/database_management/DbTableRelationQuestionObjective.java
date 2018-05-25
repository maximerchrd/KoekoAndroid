package com.LearningTracker.LearningTrackerApp.database_management;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableRelationQuestionObjective {
    private static String tableName = "question_objective_relation";
    private static String key_idGlobal = "ID_GLOBAL";
    private static String key_objective = "OBJECTIVE";

    public static String getTableName() {
        return tableName;
    }
    public static String getKey_idGlobal() {
        return key_idGlobal;
    }
    public static String getKey_objective() {
        return key_objective;
    }

    static public void createTableRelationQuestionObjectives() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                    " (ID_OBJ_REL       INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    key_idGlobal + " INT     NOT NULL, " +
                    key_objective + "  TEXT     NOT NULL) ";
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
            String sql = 	"INSERT INTO " + tableName + " (" + key_idGlobal +", " + key_objective +") " +
                    "VALUES ('" + id_global + "','" + objective.replace("'", "''") + "');";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
