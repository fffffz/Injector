package com.fffz.injector.sample;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackWriter {

    private final Context context;
    private final File trackLogFile;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public TrackWriter(Context context) {
        this.context = context;
        File cacheDir = new File(context.getExternalCacheDir(), "track");
        cacheDir.mkdirs();
        trackLogFile = new File(cacheDir, new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
    }

    public void write(String log) {
        FileWriter fw = null;
        BufferedWriter bf = null;
        try {
            fw = new FileWriter(trackLogFile, trackLogFile.exists());
            bf = new BufferedWriter(fw);
            bf.append(timeFormat.format(new Date()))
                    .append(" ")
                    .append(log)
                    .append("\n");
            bf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bf != null) {
                    bf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
