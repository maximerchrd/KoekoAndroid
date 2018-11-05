package com.wideworld.koeko.database_management;

import android.database.Cursor;

import com.wideworld.koeko.R;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableLearningObjective {
   static private String tableName = "learning_objectives";
    static private String key_objectiveId = "ID_OBJECTIVE_GLOBAL";
    static private String key_objective = "OBJECTIVE";
    static private String key_levelCognitiveAbility = "LEVEL_COGNITIVE_ABILITY";

    public static String getTableName() {
        return tableName;
    }

    public static String getKey_objectiveId() {
        return key_objectiveId;
    }

    public static String getKey_objective() {
        return key_objective;
    }

    public static String getKey_levelCognitiveAbility() {
        return key_levelCognitiveAbility;
    }

    static public void createTableLearningObjectives() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS learning_objectives " +
                    "(ID_OBJECTIVE       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_OBJECTIVE_GLOBAL      INT     NOT NULL, " +
                    " OBJECTIVE      TEXT     NOT NULL, " +
                    " LEVEL_COGNITIVE_ABILITY      INT     NOT NULL, " +
                    " UNIQUE (OBJECTIVE) ); ";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
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
    static public void addLearningObjective(String objective, int level_cognitive_ability) {
        try {
            String sql = 	"INSERT OR IGNORE INTO learning_objectives (ID_OBJECTIVE_GLOBAL,OBJECTIVE,LEVEL_COGNITIVE_ABILITY) " +
                    "VALUES ('" +
                    2000000 + "','" +
                    objective.replace("'", "''") + "','" +
                    level_cognitive_ability +"');";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
            sql = "UPDATE learning_objectives SET ID_OBJECTIVE_GLOBAL = 2000000 + ID_OBJECTIVE WHERE ID_OBJECTIVE = (SELECT MAX(ID_OBJECTIVE) FROM learning_objectives)";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public void addLearningObjective(String objectiveID, String objective, int level_cognitive_ability) {
        try {
            String sql = 	"INSERT OR IGNORE INTO learning_objectives (ID_OBJECTIVE_GLOBAL,OBJECTIVE,LEVEL_COGNITIVE_ABILITY) " +
                    "VALUES ('" +
                    objectiveID + "','" +
                    objective.replace("'", "''") + "','" +
                    level_cognitive_ability +"');";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    static public String getObjectiveWithID(String id) {
        String objective = "";
        try {
            String query = "SELECT OBJECTIVE FROM learning_objectives WHERE ID_OBJECTIVE_GLOBAL = ?";
            String[] args = {
                id
            };
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, args);
            while (cursor.moveToNext()) {
                objective = cursor.getString(0);
            }
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return objective;
    }

    static public Vector<String> getObjectivesForQuestionID(int questionID) {
        Vector<String> objectives = new Vector<>();
        try {
            String query = "SELECT OBJECTIVE FROM learning_objectives " +
                    "INNER JOIN question_objective_relation ON learning_objectives.OBJECTIVE = question_objective_relation.OBJECTIVE " +
                    "INNER JOIN multiple_choice_questions ON multiple_choice_questions.ID_GLOBAL = question_objective_relation.ID_GLOBAL " +
                    "WHERE multiple_choice_questions.ID_GLOBAL = '" + questionID + "';";
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, null);
            while (cursor.moveToNext()) {
                objectives.add(cursor.getString(0));
            }
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return objectives;
    }
    static public Vector<Vector<String>> getResultsPerObjectiveForSubject(String subject) {
        Vector<String> objectives = new Vector<>();
        Vector<String> results = new Vector<>();
        try {
            String query = "";
            if (subject.contentEquals("All")) {
                query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL FROM individual_question_for_result;";
            } else {
                query = "SELECT individual_question_for_result.ID_GLOBAL,individual_question_for_result.QUANTITATIVE_EVAL FROM individual_question_for_result " +
                        "INNER JOIN question_subject_relation ON individual_question_for_result.ID_GLOBAL=question_subject_relation.ID_GLOBAL " +
                        "INNER JOIN subjects ON question_subject_relation.ID_SUBJECT_GLOBAL=subjects.ID_SUBJECT_GLOBAL " +
                        "WHERE subjects.SUBJECT='" + subject + "';";
            }
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, null);
            Vector<String> id_questions = new Vector<>();
            Vector<String> evaluations_for_each_question = new Vector<>();
            while (cursor.moveToNext()) {
                id_questions.add(cursor.getString(0));
                evaluations_for_each_question.add(cursor.getString(1));
            }
            cursor.close();
            Vector<String> objectives_for_question = new Vector<>();
            for (int i = 0; i < id_questions.size(); i++) {
                query = "SELECT OBJECTIVE FROM question_objective_relation " +
                        "WHERE ID_GLOBAL = '" + id_questions.get(i) + "';";
                Cursor cursor2 = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, null);
                while (cursor2.moveToNext()) {
                    objectives_for_question.add(cursor2.getString(0));
                    //multiplies each evaluation for a specific question by the number of objectives attributed to the question
                    evaluations_for_each_question.insertElementAt(evaluations_for_each_question.get(objectives_for_question.size() - 1), objectives_for_question.size());
                }
                cursor2.close();
                evaluations_for_each_question.remove(objectives_for_question.size());
            }
            for (int i = 0; i < objectives_for_question.size(); i++) {
                if (!objectives.contains(objectives_for_question.get(i))) {
                    objectives.add(objectives_for_question.get(i));
                    results.add(evaluations_for_each_question.get(i));
                } else {
                    int old_result_index = objectives.indexOf(objectives_for_question.get(i));
                    double old_result = Double.parseDouble(results.get(old_result_index));
                    old_result += Double.parseDouble(evaluations_for_each_question.get(i));
                    results.set(old_result_index,String.valueOf(old_result));
                }
            }
            for (int i = 0; i < results.size(); i++) {
                double result_for_averaging = Double.parseDouble(results.get(i));
                int number_occurences = Collections.frequency(objectives_for_question,objectives.get(i));
                results.set(i,String.valueOf(result_for_averaging/number_occurences));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        //remove empty objective
        for (int i = 0; i < objectives.size(); i++) {
            if (objectives.get(i).contentEquals("") || objectives.get(i).contentEquals(" ")) {
                results.remove(i);
                objectives.remove(i);
                i--;
            }
        }
        Vector<Vector<String>> vectors = new Vector<Vector<String>>();
        vectors.add(objectives);
        vectors.add(results);
        return vectors;
    }

    static public Vector<Vector<String>> getResultsPerObjectiveForTest(String test) {
        Vector<String> objectives = new Vector<>();
        Vector<String> results = new Vector<>();
        try {
            String query = "";
            Cursor cursor;
            if (test.contentEquals("All")) {
                query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL FROM individual_question_for_result;";
                cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, null);
            } else {
                query = "SELECT " + DbTableRelationQuestionQuestion.getTableName() + "." + DbTableRelationQuestionQuestion.getKey_idGlobal1() + "," +
                        DbTableRelationQuestionQuestion.getTableName() + "." + DbTableRelationQuestionQuestion.getKey_idGlobal2() + " FROM " + DbTableRelationQuestionQuestion.getTableName() +
                        " WHERE " + DbTableRelationQuestionQuestion.getKey_testName() + " = ?";
                cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, new String[]{test});
            }

            Set<String> questionIDs = new LinkedHashSet<>();
            while (cursor.moveToNext()) {
                questionIDs.add(cursor.getString(0));
                questionIDs.add(cursor.getString(1));
            }
            cursor.close();

            Vector<String> id_questions = new Vector<>();
            Vector<String> evaluations_for_each_question = new Vector<>();
            for (String questID : questionIDs) {
                String sql = "SELECT individual_question_for_result.ID_GLOBAL, individual_question_for_result.QUANTITATIVE_EVAL FROM individual_question_for_result " +
                        " WHERE individual_question_for_result.ID_GLOBAL = ?" ;
                Cursor cursor2 = DbHelper.dbHelperSingleton.getDatabase().rawQuery(sql, new String[]{questID});
                while (cursor2.moveToNext()) {
                    id_questions.add(cursor2.getString(0));
                    evaluations_for_each_question.add(cursor2.getString(1));
                }
                cursor2.close();
            }

            Vector<String> objectives_for_question = new Vector<>();
            for (int i = 0; i < id_questions.size(); i++) {
                query = "SELECT OBJECTIVE FROM question_objective_relation " +
                        "WHERE ID_GLOBAL = '" + id_questions.get(i) + "';";
                Cursor cursor2 = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, null);
                while (cursor2.moveToNext()) {
                    objectives_for_question.add(cursor2.getString(0));
                    //multiplies each evaluation for a specific question by the number of objectives attributed to the question
                    evaluations_for_each_question.insertElementAt(evaluations_for_each_question.get(objectives_for_question.size() - 1), objectives_for_question.size());
                }
                cursor2.close();
                evaluations_for_each_question.remove(objectives_for_question.size());
            }
            for (int i = 0; i < objectives_for_question.size(); i++) {
                if (!objectives.contains(objectives_for_question.get(i))) {
                    objectives.add(objectives_for_question.get(i));
                    results.add(evaluations_for_each_question.get(i));
                } else {
                    int old_result_index = objectives.indexOf(objectives_for_question.get(i));
                    double old_result = Double.parseDouble(results.get(old_result_index));
                    old_result += Double.parseDouble(evaluations_for_each_question.get(i));
                    results.set(old_result_index,String.valueOf(old_result));
                }
            }
            for (int i = 0; i < results.size(); i++) {
                double result_for_averaging = Double.parseDouble(results.get(i));
                int number_occurences = Collections.frequency(objectives_for_question,objectives.get(i));
                results.set(i,String.valueOf(result_for_averaging/number_occurences));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        //remove empty objective
        for (int i = 0; i < objectives.size(); i++) {
            if (objectives.get(i).contentEquals("") || objectives.get(i).contentEquals(" ")) {
                results.remove(i);
                objectives.remove(i);
                i--;
            }
        }
        Vector<Vector<String>> vectors = new Vector<Vector<String>>();
        vectors.add(objectives);
        vectors.add(results);
        return vectors;
    }

    /**
     * gets the results per objective for the certificative test and the formative evaluations
     * of the objectives linked to this test
     * @param test
     * @return a vector containing 3 vectors as:
     * vector 1: objectives names
     * vector 2: corresponding certificative results
     * vector 3: corresponding formative results (-1 if no result)
     */
    static public Vector<Vector<String>> getResultsPerObjectiveForCertificativeTest(String test) {
        Vector<String> objectives = new Vector<>();
        Vector<String> certificativeResults = new Vector<>();
        Vector<String> formativeResults = new Vector<>();

        String query = "SELECT ID_GLOBAL, QUANTITATIVE_EVAL FROM individual_question_for_result" +
                " WHERE TEST_BELONGING = '" + test + "'";
        Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query, null);


        Vector<String> objectiveIds = new Vector<>();
        while (cursor.moveToNext()) {
            objectiveIds.add(cursor.getString(0));
            certificativeResults.add(cursor.getString(1));
        }
        cursor.close();

        for (String objectiveId : objectiveIds) {
            //fetch the certificative results
            String query2 = "SELECT " + key_objective + " FROM " + tableName + " WHERE " + key_objectiveId + " = '" + objectiveId + "'";
            Cursor cursor2 = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query2, null);

            if (cursor2.moveToFirst()) {
                objectives.add(cursor2.getString(0));
            } else {
                certificativeResults.remove(objectiveIds.indexOf(objectiveId));
            }
            cursor2.close();

            //fetch the formative results
            //fetch the certificative results
            Vector<String> questionsResults = new Vector<>();
            if (objectives.size() > 0) {
                String query3 = "SELECT QUANTITATIVE_EVAL FROM individual_question_for_result " +
                        " INNER JOIN question_objective_relation ON question_objective_relation.ID_GLOBAL = individual_question_for_result.ID_GLOBAL " +
                        " WHERE question_objective_relation.OBJECTIVE = '" + objectives.lastElement() + "'";
                Cursor cursor3 = DbHelper.dbHelperSingleton.getDatabase().rawQuery(query3, null);

                while (cursor3.moveToNext()) {
                    questionsResults.add(cursor3.getString(0));
                }
                cursor3.close();
            }
            Double resultsSum = 0.0;
            for (String questionResult : questionsResults) {
                resultsSum += Double.valueOf(questionResult);
            }
            Double objectiveResult;
            if (questionsResults.size() > 0) {
                objectiveResult = resultsSum / questionsResults.size();
            } else {
                objectiveResult = -1.0;
            }
            formativeResults.add(String.valueOf(objectiveResult));
        }
        Vector<Vector<String>> vectors = new Vector<>();
        vectors.add(objectives);
        vectors.add(certificativeResults);
        vectors.add(formativeResults);
        return vectors;
    }
}
