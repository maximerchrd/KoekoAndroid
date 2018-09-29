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
public class DbTableSubject {
    static public void createTableSubject() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS subjects " +
                    "(ID_SUBJECT       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " ID_SUBJECT_GLOBAL      INT     NOT NULL, " +
                    " SUBJECT           TEXT    NOT NULL, UNIQUE (SUBJECT)); ";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public void addSubject(String subject) {
        if (!subject.contentEquals("")) {
            try {
                String sql = "INSERT OR IGNORE INTO subjects (ID_SUBJECT_GLOBAL,SUBJECT) " +
                        "VALUES ('" +
                        2000000 + "','" +
                        subject + "');";
                DbHelper.dbase.execSQL(sql);
                sql = "UPDATE subjects SET ID_SUBJECT_GLOBAL = 2000000 + ID_SUBJECT WHERE ID_SUBJECT = (SELECT MAX(ID_SUBJECT) FROM subjects);";
                DbHelper.dbase.execSQL(sql);
            } catch (Exception e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }
    }
    static public Vector<String> getSubjectsForQuestionID(Long questionID) {
        Vector<String> subjects = new Vector<>();
        try {
            String query = "SELECT SUBJECT FROM subjects " +
                    "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                    "INNER JOIN multiple_choice_questions ON multiple_choice_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                    "WHERE multiple_choice_questions.ID_GLOBAL = '" + questionID + "';";
            Cursor cursor = DbHelper.dbase.rawQuery(query, null);
            while (cursor.moveToNext()) {
                subjects.add(cursor.getString(0));
            }
            query = "SELECT SUBJECT FROM subjects " +
                    "INNER JOIN question_subject_relation ON subjects.ID_SUBJECT_GLOBAL = question_subject_relation.ID_SUBJECT_GLOBAL " +
                    "INNER JOIN short_answer_questions ON short_answer_questions.ID_GLOBAL = question_subject_relation.ID_GLOBAL " +
                    "WHERE short_answer_questions.ID_GLOBAL = '" + questionID + "';";
            Cursor cursor2 = DbHelper.dbase.rawQuery(query, null);
            while (cursor2.moveToNext()) {
                subjects.add(cursor2.getString(0));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return subjects;
    }
    static public Vector<String> getAllSubjects() {
        Vector<String> subjects = new Vector<>();
        try {
            String query = "SELECT SUBJECT FROM subjects;";
            Cursor cursor = DbHelper.dbase.rawQuery(query, null);
            while (cursor.moveToNext()) {
                subjects.add(cursor.getString(0));
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return subjects;
    }

    static public Vector<Vector<String>> getSubjectsAndQuestionsNeedingPractice() {
        Vector<Vector<String>> questionIDsAndSubjects = new Vector<>();
        Vector<String> questionIDs = new Vector<>();
        Vector<String> questionEvals = new Vector<>();
        Vector<String> subjects = new Vector<>();
        try {
            //get all question IDs and corresponding evaluations
            String query = "SELECT ID_GLOBAL,QUANTITATIVE_EVAL FROM individual_question_for_result WHERE TYPE IS NOT 2;";
            Cursor cursor = DbHelper.dbase.rawQuery(query, null);
            while (cursor.moveToNext()) {
                questionIDs.add(cursor.getString(0));
                questionEvals.add(cursor.getString(1));
            }
            cursor.close();
            //keep only the latest results of each question
            int arraysLength = questionIDs.size();
            Vector<String> singleGlobalIDs = new Vector<>();
            for (int i = 0; i < arraysLength; i++) {
                if (singleGlobalIDs.contains(questionIDs.get(i))) {
                    int indexOfObjectToRemove = singleGlobalIDs.indexOf(questionIDs.get(i));
                    questionEvals.remove(indexOfObjectToRemove);
                    questionIDs.remove(indexOfObjectToRemove);
                    singleGlobalIDs.remove(indexOfObjectToRemove);
                    i--;
                    arraysLength--;
                }
                singleGlobalIDs.add(questionIDs.get(i));
            }

            //remove the questions with good evaluations
            arraysLength = questionIDs.size();
            for (int i = 0; i < arraysLength; i++) {
                if (Double.valueOf(questionEvals.get(i)) > 90) {
                    questionEvals.remove(i);
                    questionIDs.remove(i);
                    i--;
                    arraysLength--;
                }
            }

            questionIDsAndSubjects.add(questionIDs);

            //get the subjects for the remaining questions IDs
            for (int i = 0; i < questionIDs.size(); i++) {
                Vector<String> tempSubjectVector = new Vector<>();
                tempSubjectVector = getSubjectsForQuestionID(Long.valueOf(questionIDs.get(i)));
                for (int j = 0; j < tempSubjectVector.size(); j++) {
                    subjects.add(tempSubjectVector.get(j));
                }
            }

            //remove double subjects
            arraysLength = subjects.size();
            Vector<String> singleSubject = new Vector<>();
            for (int i = 0; i < arraysLength; i++) {
                if (singleSubject.contains(subjects.get(i))) {
                    int indexOfObjectToRemove = singleSubject.indexOf(subjects.get(i));
                    subjects.remove(indexOfObjectToRemove);
                    singleSubject.remove(indexOfObjectToRemove);
                    i--;
                    arraysLength--;
                }
                singleSubject.add(subjects.get(i));
            }

            questionIDsAndSubjects.add(subjects);

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return questionIDsAndSubjects;
    }
}
