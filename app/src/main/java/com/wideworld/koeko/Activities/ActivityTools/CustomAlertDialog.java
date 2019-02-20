package com.wideworld.koeko.Activities.ActivityTools;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;

public class CustomAlertDialog extends Dialog {
    private Context context;
    private TextView correctionMessage;
    private ImageView medalImageView;
    private Button okButton;
    private Activity activity;
    private Boolean testInstructions;
    private String medal;
    private String message;

    public CustomAlertDialog(Context context) {
        super(context);
        this.context = context;
        this.testInstructions = false;
        this.medal = "none";
    }

    public CustomAlertDialog(Activity activity, String message, String medal) {
        super(activity);
        this.context = activity;
        this.testInstructions = false;
        this.medal = medal;
        this.message = message;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.correction_alertdialog);
        correctionMessage = findViewById(R.id.correction_text);
        if (message != null) {
            correctionMessage.setText(message);
        }

        medalImageView = findViewById(R.id.medal_image);
        if (medal.contentEquals("none")) {
            medalImageView.getLayoutParams().height = 0;
        } else if (medal.contentEquals("bronze-medal")) {
            Drawable drawable  = context.getResources().getDrawable(R.drawable.bronze_medal);
            medalImageView.setImageDrawable(drawable);
        } else if (medal.contentEquals("silver-medal")) {
            Drawable drawable  = context.getResources().getDrawable(R.drawable.silver_medal);
            medalImageView.setImageDrawable(drawable);
        } else if (medal.contentEquals("gold-medal")) {
            Drawable drawable  = context.getResources().getDrawable(R.drawable.gold_medal);
            medalImageView.setImageDrawable(drawable);
        }
        okButton = findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {
                Log.v("Correction dialog: ", "focus lost");
                ((Koeko) context.getApplicationContext()).startActivityTransitionTimer();
                dismiss();
                if (!testInstructions) {
                    activity.finish();
                    activity.invalidateOptionsMenu();
                } else {
                    if (Koeko.currentTestFragmentSingleton.testChronometer != null) {
                        Koeko.currentTestFragmentSingleton.testChronometer.reset();
                        Koeko.currentTestFragmentSingleton.testChronometer.run();
                        Koeko.activeTestStartTime = Koeko.currentTestFragmentSingleton.testChronometer.getStartTime();
                    }
                }
            }
        });
    }

    public void setProperties(String message, Activity activity) {
        correctionMessage.setText(message);
        this.activity = activity;
    }

    public Boolean getTestInstructions() {
        return testInstructions;
    }

    public void setTestInstructions(Boolean testInstructions) {
        this.testInstructions = testInstructions;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("Correction dialog: ", "focus lost");
            ((Koeko) context.getApplicationContext()).startActivityTransitionTimer();
        } else {
            ((Koeko) context.getApplicationContext()).stopActivityTransitionTimer();
            Log.v("Correction dialog: ", "has focus");
        }
    }
}
