package com.wideworld.koeko.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;
import com.wideworld.koeko.Tools.FileHandler;

import java.io.File;



public class WebViewFragment extends Fragment {
    private WebView testWebview;

    static private String TAG = "WebViewFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_webview, container, false);
        testWebview = rootView.findViewById(R.id.test_webview);

        File directory = new File(getActivity().getFilesDir(), FileHandler.mediaDirectoryNoSlash);
        File file = new File(directory, Koeko.currentTestFragmentSingleton.getmTest().getMediaFileName());

        testWebview.getSettings().setJavaScriptEnabled(true);
        testWebview.loadUrl("file:///" + file.getPath());
        return rootView;
    }
}
