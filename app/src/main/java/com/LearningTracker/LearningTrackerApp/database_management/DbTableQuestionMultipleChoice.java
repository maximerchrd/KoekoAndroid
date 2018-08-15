package com.LearningTracker.LearningTrackerApp.database_management;

import android.database.Cursor;
import android.util.Log;

import com.LearningTracker.LearningTrackerApp.QuestionsManagement.QuestionMultipleChoice;

import java.sql.PreparedStatement;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableQuestionMultipleChoice {
    static public void createTableQuestionMultipleChoice() {
        try {
            System.out.println("creating table multiple choice");
            String sql = "CREATE TABLE IF NOT EXISTS multiple_choice_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " LEVEL      INT     NOT NULL, " +
                    " QUESTION           TEXT    NOT NULL, " +
                    " OPTION0           TEXT    NOT NULL, " +
                    " OPTION1           TEXT    NOT NULL, " +
                    " OPTION2           TEXT    NOT NULL, " +
                    " OPTION3           TEXT    NOT NULL, " +
                    " OPTION4           TEXT    NOT NULL, " +
                    " OPTION5           TEXT    NOT NULL, " +
                    " OPTION6           TEXT    NOT NULL, " +
                    " OPTION7           TEXT    NOT NULL, " +
                    " OPTION8           TEXT    NOT NULL, " +
                    " OPTION9           TEXT    NOT NULL, " +
                    " NB_CORRECT_ANS        INT     NOT NULL, " +
                    " IMAGE_PATH           TEXT    NOT NULL, " +
                    " ID_GLOBAL           INT    NOT NULL, " +
                    " UNIQUE(ID_GLOBAL)) ";
            DbHelper.dbase.execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * method for inserting new question into table multiple_choice_question
     * @param quest
     * @throws Exception
     */
    static public void addMultipleChoiceQuestion(QuestionMultipleChoice quest) throws Exception {
        try {
            String sql = 	"INSERT OR REPLACE INTO multiple_choice_questions (LEVEL,QUESTION,OPTION0," +
                    "OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9," +
                    "NB_CORRECT_ANS,IMAGE_PATH,ID_GLOBAL) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            String[] sqlArgs = new String[]{
                    quest.getLEVEL(),
                    quest.getQUESTION(),
                    quest.getOPT0(),
                    quest.getOPT1(),
                    quest.getOPT2(),
                    quest.getOPT3(),
                    quest.getOPT4(),
                    quest.getOPT5(),
                    quest.getOPT6(),
                    quest.getOPT7(),
                    quest.getOPT8(),
                    quest.getOPT9(),
                    String.valueOf(quest.getNB_CORRECT_ANS()),
                    quest.getIMAGE(),
                    String.valueOf(quest.getID())
            };

            DbHelper.dbase.execSQL(sql,sqlArgs);
            Log.v("insert multQuest, ID: ", String.valueOf(quest.getID()));
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
    static public QuestionMultipleChoice getQuestionWithId(String globalID) {
        QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
        try {
            String selectQuery = "SELECT  LEVEL,QUESTION,OPTION0,OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9,NB_CORRECT_ANS," +
                    "IMAGE_PATH FROM multiple_choice_questions WHERE ID_GLOBAL=" + globalID + ";";
            //DbHelper.dbase = DbHelper.getReadableDatabase();
            Cursor cursor = DbHelper.dbase.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToPosition(0)) {
                questionMultipleChoice.setLEVEL(cursor.getString(0));
                questionMultipleChoice.setQUESTION(cursor.getString(1));
                questionMultipleChoice.setOPT0(cursor.getString(2));
                questionMultipleChoice.setOPT1(cursor.getString(3));
                questionMultipleChoice.setOPT2(cursor.getString(4));
                questionMultipleChoice.setOPT3(cursor.getString(5));
                questionMultipleChoice.setOPT4(cursor.getString(6));
                questionMultipleChoice.setOPT5(cursor.getString(7));
                questionMultipleChoice.setOPT6(cursor.getString(8));
                questionMultipleChoice.setOPT7(cursor.getString(9));
                questionMultipleChoice.setOPT8(cursor.getString(10));
                questionMultipleChoice.setOPT9(cursor.getString(11));
                questionMultipleChoice.setNB_CORRECT_ANS(Integer.valueOf(cursor.getString(12)));
                questionMultipleChoice.setIMAGE(cursor.getString(13));
            }
            questionMultipleChoice.setID(globalID);
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return questionMultipleChoice;
    }

    static public String getAllQuestionMultipleChoiceIds() {
        String IDs = "";

        try {
            String selectQuery = "SELECT ID_GLOBAL FROM multiple_choice_questions;";
            //DbHelper.dbase = DbHelper.getReadableDatabase();
            Cursor cursor = DbHelper.dbase.rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            while (cursor.moveToNext()) {
                IDs += cursor.getString(0) + "|";
            }
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return IDs;
    }
}
