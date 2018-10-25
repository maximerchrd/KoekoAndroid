package com.wideworld.koeko.Tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHandler {
    static public String mediaDirectory = "media";

    static public void saveMediaFile(byte[] fileData, String fileName, Context context) {
        File directory = new File(context.getFilesDir(), mediaDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(fileData);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public String getMediaFilesList(Context context) {
        String list = "";
        File directory = new File(context.getFilesDir(), mediaDirectory);
        File[] files = directory.listFiles();
        for (File inFile : files) {
            if (!inFile.isDirectory()) {
                list += inFile.getName() + "|";
            }
        }

        return list;
    }
}
