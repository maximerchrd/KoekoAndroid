package com.wideworld.koeko.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;
import com.wideworld.koeko.Tools.FileHandler;

import java.io.File;



public class WebViewActivity extends AppCompatActivity {
    private WebView testWebview;

    static private String TAG = "WebViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_webview);
        testWebview = findViewById(R.id.test_webview);

        File directory = new File(getApplicationContext().getFilesDir(), FileHandler.mediaDirectoryNoSlash);
        File file = new File(directory, Koeko.currentTestActivitySingleton.getmTest().getMediaFileName());

        testWebview.getSettings().setJavaScriptEnabled(true);
        testWebview.loadUrl("file:///" + file.getPath());

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.v(TAG, "onStart");
        Koeko.MAX_ACTIVITY_TRANSITION_TIME_MS = Koeko.SHORT_TRANSITION_TIME;
        super.onStart();
    }

    /**
     * method used to know if we send a disconnection signal to the server
     *
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("webview activity: ", "focus lost");
            ((Koeko) this.getApplication()).startActivityTransitionTimer();
        } else {
            ((Koeko) this.getApplication()).stopActivityTransitionTimer();
            Log.v("webview activity: ", "has focus");
        }
    }
}
