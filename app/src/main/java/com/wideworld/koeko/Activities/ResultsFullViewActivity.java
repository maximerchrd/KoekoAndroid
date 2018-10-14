package com.wideworld.koeko.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wideworld.koeko.R;

import java.io.File;

public class ResultsFullViewActivity extends Activity {

    private TextView questionTextView;
    private TextView studentAnswerTextView;
    private TextView allAnswersTextView;
    private TextView dateTextView;
    private TextView evaluationTextView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_results_fullview);

        questionTextView = findViewById(R.id.result_full_questiontext);
        studentAnswerTextView = findViewById(R.id.result_full_studentanswertext);
        allAnswersTextView = findViewById(R.id.result_full_allanswerstext);
        dateTextView = findViewById(R.id.result_full_date);
        evaluationTextView = findViewById(R.id.result_full_evaluation);
        imageView = findViewById(R.id.result_full_image);

        Bundle bun = getIntent().getExtras();
        questionTextView.setText(bun.getString("questionText"));
        studentAnswerTextView.setText(bun.getString("studentAnswer"));
        allAnswersTextView.setText(bun.getString("allAnswers"));
        dateTextView.setText(bun.getString("date"));
        evaluationTextView.setText(bun.getString("evaluation"));

        File imgFile = new  File(getFilesDir()+"/images/" + bun.getString("questionImage"));
        if(imgFile.exists()){
            String path = imgFile.getAbsolutePath();
            Bitmap myBitmap = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(myBitmap);
            imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
        }
    }
}
