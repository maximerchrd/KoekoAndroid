package com.wideworld.koeko.Activities.ActivityTools;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;
import com.wideworld.koeko.database_management.DbTableSettings;

import java.util.ArrayList;

public class HomeworkCodeAlertDialog extends Dialog {
    private Context context;
    private TextView homeworkCodeTextView;
    private EditText homeworkCodeEditText;
    private TextView friendlyNameTextView;
    private EditText friendlyNameEditText;
    private Button okButton;
    private Activity activity;

    private Spinner spinner;
    private ArrayAdapter<String> homeworkKeysAdapter;
    private ArrayList<String> spinnerList;
    private ArrayList<String> originalSpinnerList;

    private String homeworkCode;
    private String friendlyName;

    public HomeworkCodeAlertDialog(Context context, String homeworkCode, String friendlyName, ArrayAdapter<String> homeworkKeysAdapter,
                                   ArrayList<String> spinnerList, ArrayList<String> originalSpinnerList,
                                   Spinner spinner) {
        super(context);
        this.context = context;
        this.homeworkCode = homeworkCode;
        this.friendlyName = friendlyName;
        this.homeworkKeysAdapter = homeworkKeysAdapter;
        this.spinnerList = spinnerList;
        this.originalSpinnerList = originalSpinnerList;
        this.spinner = spinner;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.homeworkcode_alertdialog);

        homeworkCodeEditText = findViewById(R.id.homework_code_edittext);
        friendlyNameEditText = findViewById(R.id.homework_friendlyname_edittext);

        homeworkCodeEditText.setText(homeworkCode);
        friendlyNameEditText.setText(friendlyName);

        okButton = findViewById(R.id.homework_save_key);
        okButton.setOnClickListener(v -> {
            if (homeworkCodeEditText.getText().toString().length() > 0) {
                if (this.homeworkCode.length() == 0) {
                    DbTableSettings.insertHomeworkKey(homeworkCodeEditText.getText().toString(), friendlyNameEditText.getText().toString());
                    updateSpinner();
                } else {
                    DbTableSettings.updateHomeworkKey(this.homeworkCode, this.friendlyName,
                            homeworkCodeEditText.getText().toString(), friendlyNameEditText.getText().toString());
                    updateSpinner();
                }
                spinner.setSelection(spinnerList.size() - 1);
                dismiss();
            }
        });
    }

    private void updateSpinner() {
        originalSpinnerList.add(homeworkCodeEditText.getText().toString().replace("/", "") + "/" +
                friendlyNameEditText.getText().toString().replace("/", ""));
        if (friendlyNameEditText.getText().toString().length() == 0) {
            spinnerList.add(homeworkCodeEditText.getText().toString());
        } else {
            spinnerList.add(friendlyNameEditText.getText().toString());
        }
        homeworkKeysAdapter.notifyDataSetChanged();
    }
}