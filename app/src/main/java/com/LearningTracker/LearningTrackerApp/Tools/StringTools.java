package com.LearningTracker.LearningTrackerApp.Tools;

import android.util.Log;

public class StringTools {
    static public Long stringToLongID(String string) {
        Long longId = 0L;

        try {
            longId = Long.valueOf(string);
        } catch (NumberFormatException e) {
            longId = 0L;
            Log.e("StringTools", "error converting string(= " + string + " ) to long for identifier");
        }

        if (string.length() > 15) {
            Log.e("StringTools", "string(= " + string + " ) ID too long");
            return 0L;
        }

        return longId;
    }
}
