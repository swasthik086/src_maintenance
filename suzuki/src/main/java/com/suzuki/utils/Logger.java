package com.suzuki.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final String TAG = "MyApp";
    private final Context context;

    public Logger(Context context){
        this.context = context;
    }
    public static void log(Context context,String message) {
        Log.d(TAG, message);
        saveLogToFile(context,message);
    }

    private static void saveLogToFile(Context context, String message) {

        Log.e("coming","coming");
        String logFileName = "file(test_5).txt";
        //   File logFile = new File(Context.getExternalFilesDir(null), logFileName);
        File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File logFile = new File(externalStorageDir, logFileName);

        try {
            Toast.makeText(context, "entered", Toast.LENGTH_SHORT).show();
            FileWriter writer = new FileWriter(logFile, true);
            String logLine = getCurrentTimestamp() + " " + message + "\n";
            writer.append(logLine);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
