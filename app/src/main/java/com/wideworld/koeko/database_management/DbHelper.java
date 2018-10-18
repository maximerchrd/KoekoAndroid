package com.wideworld.koeko.database_management;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.wideworld.koeko.R;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {
    static public DbHelper dbHelperSingleton;

    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "learning_tracker.db";  //added the ".db" extension because onCreate() wasn't called anymore

    // old table name
    private static final String TABLE_QUEST = "quest";

    private SQLiteDatabase dbase;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbHelperSingleton = this;
        dbase = getWritableDatabase();
        createTables(context);
        Log.v("DbHelper", "Constructor");
    }

    public SQLiteDatabase getDatabase() {
        if (dbase != null) {
            return dbase;
        } else {
            Log.w("DbHelper", "dbase is null when should be already initialized! BAD!!");
            dbase = getWritableDatabase();
            return dbase;
        }
    }

    private void createTables(Context context) {
        DbTableSettings.createTable(context.getString(R.string.no_name));
        DbTableQuestionMultipleChoice.createTableQuestionMultipleChoice();
        DbTableLearningObjective.createTableLearningObjectives();
        DbTableRelationQuestionObjective.createTableRelationQuestionObjectives();
        DbTableIndividualQuestionForResult.createTableIndividualQuestionForResult();
        DbTableQuestionShortAnswer.createTableQuestionShortAnswer();
        DbTableSubject.createTableSubject();
        DbTableRelationQuestionSubject.createTableSubject();
        DbTableAnswerOptions.createTableAnswerOptions();
        DbTableRelationQuestionAnserOption.createTableSubject();
        DbTableTest.createTableTest();
        DbTableRelationTestObjective.createTable();
        DbTableRelationQuestionQuestion.createTable();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEST);
        // Create tables again
        onCreate(db);
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }
}
