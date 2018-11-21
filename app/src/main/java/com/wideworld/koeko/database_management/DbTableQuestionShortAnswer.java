package com.wideworld.koeko.database_management;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.wideworld.koeko.QuestionsManagement.QuestionShortAnswer;

import java.util.ArrayList;

/**
 * Created by maximerichard on 03.01.18.
 */
public class DbTableQuestionShortAnswer {
    static public void createTableQuestionShortAnswer() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS short_answer_questions " +
                    "(ID_QUESTION       INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " LEVEL      INT     NOT NULL, " +
                    " QUESTION           TEXT    NOT NULL, " +
                    " IMAGE_PATH           TEXT    NOT NULL, " +
                    " ID_GLOBAL           INT    NOT NULL, " +
                    " MODIF_DATE       TEXT, " +
                    " HASH_CODE       TEXT, " +
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
     * @param quest
     * @throws Exception
     */
    static public void addShortAnswerQuestion(QuestionShortAnswer quest) {
        try {
            String sql = 	"INSERT OR REPLACE INTO short_answer_questions (LEVEL,QUESTION,IMAGE_PATH,ID_GLOBAL, MODIF_DATE, IDENTIFIER, HASH_CODE) " +
                    "VALUES (?,?,?,?,?,?,?)";
            String[] sqlArgs = new String[]{
                    quest.getLevel(),
                    quest.getQuestion(),
                    quest.getImage(),
                    quest.getId(),
                    quest.getModifDate(),
                    quest.getIdentifier(),
                    quest.getHashCode()
            };
            DbHelper.dbHelperSingleton.getDatabase().execSQL(sql,sqlArgs);
            Log.v("insert shrtaQuest, ID: ", String.valueOf(quest.getId()));

            for (int i = 0; i < quest.getAnswers().size(); i++) {
                DbTableAnswerOptions.addAnswerOption(String.valueOf(quest.getId()),quest.getAnswers().get(i));
            }
        } catch ( SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    static public QuestionShortAnswer getShortAnswerQuestionWithId(String globalID) {
        QuestionShortAnswer questionShortAnswer = new QuestionShortAnswer();
        try {
            String selectQuery = "SELECT  LEVEL,QUESTION,IMAGE_PATH FROM short_answer_questions WHERE ID_GLOBAL=" + globalID + ";";
            //DbHelper.dbHelperSingleton.getDatabase() = DbHelper.getReadableDatabase();
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            if (cursor.moveToPosition(0)) {
                questionShortAnswer.setLevel(cursor.getString(0));
                questionShortAnswer.setQuestion(cursor.getString(1));
                questionShortAnswer.setImage(cursor.getString(2));
            }
            questionShortAnswer.setId(globalID);
            cursor.close();

            //get answers
            ArrayList<String> answers = new ArrayList<>();
            selectQuery = "SELECT OPTION FROM answer_options " +
                    "INNER JOIN question_answeroption_relation ON answer_options.ID_ANSWEROPTION_GLOBAL = question_answeroption_relation.ID_ANSWEROPTION_GLOBAL " +
                    "INNER JOIN short_answer_questions ON question_answeroption_relation.ID_GLOBAL = short_answer_questions.ID_GLOBAL " +
                    "WHERE short_answer_questions.ID_GLOBAL = '" + globalID +"';";
            Cursor cursor2 = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
            while ( cursor2.moveToNext() ) {
                answers.add(cursor2.getString(0));
            }
            questionShortAnswer.setAnswers(answers);
            cursor2.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return questionShortAnswer;
    }

    static public String getAllShortAnswerIdsAndHashCode() {
        String IDs = "";
        try {
            String selectQuery = "SELECT ID_GLOBAL, HASH_CODE FROM short_answer_questions;";
            //DbHelper.dbHelperSingleton.getDatabase() = DbHelper.getReadableDatabase();
            Cursor cursor = DbHelper.dbHelperSingleton.getDatabase().rawQuery(selectQuery, null);
            // looping through all rows and adding to list
            while (cursor.moveToNext()) {
                IDs += cursor.getString(0) + ";" + cursor.getString(1) + "|";
            }
            cursor.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return IDs;
    }
}
