package com.wideworld.koeko.database_management;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.wideworld.koeko.QuestionsManagement.QuestionMultipleChoice;
import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;
import com.wideworld.koeko.QuestionsManagement.QuestionView;
import com.wideworld.koeko.QuestionsManagement.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;

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
                    " MODIF_DATE       TEXT, " +
                    " HASH_CODE       TEXT, " +
                    " TIMER_SECONDS       INTEGER, " +
                    " IDENTIFIER        VARCHAR(15)," +
                    " UNIQUE(ID_GLOBAL)) ";
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    /**
     * method for inserting new question into table multiple_choice_question
     *
     * @param quest
     * @throws Exception
     */
    static public void addQuestionFromView(QuestionView quest) {
        if (quest.getTYPE() == 0) {
            String sql = "INSERT OR REPLACE INTO multiple_choice_questions (LEVEL,QUESTION,OPTION0," +
                    "OPTION1,OPTION2,OPTION3,OPTION4,OPTION5,OPTION6,OPTION7,OPTION8,OPTION9," +
                    "NB_CORRECT_ANS,IMAGE_PATH,ID_GLOBAL,IDENTIFIER,MODIF_DATE,HASH_CODE,TIMER_SECONDS) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
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
                    String.valueOf(quest.getID()),
                    String.valueOf(quest.getID()),
                    String.valueOf(quest.getQCM_UPD_TMS()),
                    quest.getHashCode(),
                    String.valueOf(quest.getTimerSeconds())
            };

            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql,sqlArgs);
        }
        else if (quest.getTYPE() == 1) {
            QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
            questionShortAnswer.setId(quest.getID());
            questionShortAnswer.setQuestion(quest.getQUESTION());
            questionShortAnswer.setImage(quest.getIMAGE());
            questionShortAnswer.getAnswers().add(quest.getOPT0());
            questionShortAnswer.getAnswers().add(quest.getOPT1());
            questionShortAnswer.getAnswers().add(quest.getOPT2());
            questionShortAnswer.getAnswers().add(quest.getOPT3());
            questionShortAnswer.getAnswers().add(quest.getOPT4());
            questionShortAnswer.getAnswers().add(quest.getOPT5());
            questionShortAnswer.getAnswers().add(quest.getOPT6());
            questionShortAnswer.getAnswers().add(quest.getOPT7());
            questionShortAnswer.getAnswers().add(quest.getOPT8());
            questionShortAnswer.getAnswers().addAll(new ArrayList<>(Arrays.asList(quest.getOPT9().split("///"))));
            questionShortAnswer.setModifDate(quest.getQCM_UPD_TMS().toString());
            questionShortAnswer.setIdentifier(quest.getQCM_MUID());
            questionShortAnswer.setHashCode(quest.getHashCode());
            questionShortAnswer.setTimerSeconds(quest.getTimerSeconds());

            DbTableQuestionShortAnswer.addShortAnswerQuestion(questionShortAnswer);
        } else if (quest.getTYPE() == 2) {
//            Test test = new Test();
//            test.setIdGlobal(quest.getQCM_MUID());
//            test.setTestName(quest.getQUESTION());
//
//            DbTableTest.addTest(test);
            System.err.println("ERROR inserting question into db: we are not supposed to have a type 2 (test)");
        }
    }

    static public QuestionMultipleChoice getQuestionWithId(String globalID) {
        QuestionMultipleChoice questionMultipleChoice = new QuestionMultipleChoice();
        try {
            String selectQuery = "SELECT  LEVEL,QUESTION,OPTION0,OPTION1,OPTION2,OPTION3,OPTION4,OPTION5," +
                    "OPTION6,OPTION7,OPTION8,OPTION9,NB_CORRECT_ANS," +
                    "IMAGE_PATH,TIMER_SECONDS FROM multiple_choice_questions WHERE ID_GLOBAL=" + globalID + ";";
            //DbHelper.dbHelperSingleton.getDatabase() = DbHelper.getReadableDatabase();
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToPosition(0)) {
                questionMultipleChoice.setLevel(cursor.getString(0));
                questionMultipleChoice.setQuestion(cursor.getString(1));
                questionMultipleChoice.setOpt0(cursor.getString(2));
                questionMultipleChoice.setOpt1(cursor.getString(3));
                questionMultipleChoice.setOpt2(cursor.getString(4));
                questionMultipleChoice.setOpt3(cursor.getString(5));
                questionMultipleChoice.setOpt4(cursor.getString(6));
                questionMultipleChoice.setOpt5(cursor.getString(7));
                questionMultipleChoice.setOpt6(cursor.getString(8));
                questionMultipleChoice.setOpt7(cursor.getString(9));
                questionMultipleChoice.setOpt8(cursor.getString(10));
                questionMultipleChoice.setOpt9(cursor.getString(11));
                questionMultipleChoice.setNB_CORRECT_ANS(Integer.valueOf(cursor.getString(12)));
                questionMultipleChoice.setImage(cursor.getString(13));
                questionMultipleChoice.setTimerSeconds(cursor.getInt(14));
            }
            questionMultipleChoice.setId(globalID);
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return questionMultipleChoice;
    }

    static public Boolean checkIfIdMatchResource(String id) {
        String sql = "SELECT * FROM multiple_choice_questions WHERE ID_GLOBAL=?";
        try {
            //check for multiple choice question
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(sql, new String[]{id});
            if (cursor.moveToNext()) {
                return true;
            }

            //check for short answer question
            sql = "SELECT * FROM short_answer_questions WHERE ID_GLOBAL=?";
            cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(sql, new String[]{id});
            if (cursor.moveToNext()) {
                return true;
            }

            //check for test
            sql = "SELECT * FROM " + DbTableTest.getTableName() + " WHERE " + DbTableTest.getKey_idGlobal() + "=?";
            cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(sql, new String[]{id});
            if (cursor.moveToNext()) {
                return true;
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        return false;
    }

    static public String getAllQuestionMultipleChoiceIdsAndHashCode() {
        String IDsAndUpdTime = "";

        try {
            String selectQuery = "SELECT ID_GLOBAL, HASH_CODE FROM multiple_choice_questions;";
            //DbHelper.dbHelperSingleton.getDatabase() = DbHelper.getReadableDatabase();
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            while (cursor.moveToNext()) {
                IDsAndUpdTime += cursor.getString(0) + ";" + cursor.getString(1) + "|";
            }
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

        return IDsAndUpdTime;
    }
}
