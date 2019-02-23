/***
 Copyright (c) 2013 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Android Development_
 http://commonsware.com/Android
 */


package com.wideworld.koeko.Activities.ActivityTools;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

public class TestChronometer extends android.support.v7.widget.AppCompatTextView implements Runnable {
    private long startTime = 0L;
    private long overallDuration = 0L;
    private boolean isRunning = false;

    public TestChronometer(Context context) {
        super(context);

        reset();
    }

    public TestChronometer(Context context, AttributeSet attrs) {
        super(context, attrs);

        reset();
    }

    public long getOverallDuration() {
        return overallDuration;
    }

    @Override
    public void run() {
        isRunning = true;

        long elapsedSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000;
        overallDuration = elapsedSeconds;
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;

        setText(String.format("%02d:%02d", minutes, seconds));

        postDelayed(this, 1000);
    }

    public void reset() {
        startTime = SystemClock.elapsedRealtime();
        setText("");
        setTextColor(Color.WHITE);
    }

    public void stop() {
        removeCallbacks(this);
        isRunning = false;
    }

    public boolean isRunning() {
        return (isRunning);
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
