package com.wideworld.koeko.Tools;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileHandler {
    static public String mediaDirectoryNoSlash = "media";
    static public String mediaDirectory = "media/";

    static public void saveMediaFile(byte[] fileData, String fileName, Context context) {
        File directory = new File(context.getFilesDir(), mediaDirectoryNoSlash);
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

    static public ArrayList<String> getMediaFilesList(Context context) {
        ArrayList<String> list = new ArrayList<>();
        File directory = new File(context.getFilesDir(), mediaDirectoryNoSlash);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (!inFile.isDirectory()) {
                    list.add(inFile.getName());
                }
            }
        }

        return list;
    }
}
