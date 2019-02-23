package com.wideworld.koeko.Activities.ActivityTools;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;

public class MedalFragment extends Fragment {
    ImageView medalImage;
    TextView medalText;
    Button okButton;

    static public final String noMedal = "no-medal";
    static public final String bronzeMedal = "bronze-medal";
    static public final String silverMedal = "silver-medal";
    static public final String goldMedal = "gold-medal";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_medal, container, false);
        medalImage = rootView.findViewById(R.id.medal_image_fragment);
        medalText = rootView.findViewById(R.id.medal_text);
        okButton = rootView.findViewById(R.id.medal_ok_button);

        Bundle bun = getArguments();
        String medal = bun.getString("medal");
        String text = bun.getString("text");
        Boolean testInstructions = bun.getBoolean("instructions");

        switch (medal) {
            case noMedal:
                medalImage.setVisibility(View.GONE);
                break;
            case bronzeMedal:
                medalImage.setImageResource(R.drawable.bronze_medal);
                break;
            case silverMedal:
                medalImage.setImageResource(R.drawable.silver_medal);
                break;
            case goldMedal:
                medalImage.setImageResource(R.drawable.gold_medal);
                break;
        }

        if (text != null) {
            medalText.setText(text);
        }

        okButton.setOnClickListener(v -> {
            if (testInstructions) {
                Koeko.currentTestFragmentSingleton.showChronometer();
                Koeko.currentTestFragmentSingleton.testChronometer.reset();
                Koeko.currentTestFragmentSingleton.testChronometer.run();
                Koeko.activeTestStartTime = Koeko.currentTestFragmentSingleton.testChronometer.getStartTime();
            }
            Koeko.networkCommunicationSingleton.mInteractiveModeActivity.getSupportFragmentManager().popBackStack();
        });

        return rootView;
    }
}