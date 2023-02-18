package com.seclab.rildefender.Util;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class FileUtil {

    public static String baseDir = Environment.getExternalStorageDirectory().getPath();
    // TODO switch to app's private folder
    public static String logFile = baseDir + "/Download/log.txt";
    public static String smsRecordFile = baseDir + "/Download/sms.txt";
    public static String yamlFile = baseDir + "/Download/signatures.yaml";
    public static String jsonFile = baseDir + "/Download/results.json";

    public static void writeToFile(Context context, String path, String content, boolean append) {
        try {
//            File outFile = new File(context.getFilesDir() + path);
            File outFile = new File(path);
            outFile.createNewFile(); // create if not exist
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, append)));
            out.println(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeLog(Context context, String s, boolean append) {
        try {
            File outFile = new File(logFile);
            outFile.createNewFile(); // create if not exist
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, append)));
            out.println(s);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeJSONResults(Context context, JSONArray jsonArray) {
        try {
            File outFile = new File(jsonFile);
            outFile.createNewFile(); // create if not exist
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile, false)));
            out.println(jsonArray.toString(4));
            out.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (Context context, String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public static boolean createFileIfNotExist (Context context, String filePath) throws IOException{
        File fl = new File(filePath);
        if (!fl.exists()) {
            return fl.createNewFile();
        }
        return false;
    }


    public static void recordSMSToFile(Context context, JSONObject jsonObject) throws JSONException{
        writeToFile(context, smsRecordFile, jsonObject.toString(4), true);
        writeToFile(context, smsRecordFile, ",", true);
    }

    public static JSONArray readSMSEventsFromFile(Context context) {
        try {
            String jsonStr = "[\n" + getStringFromFile(context, smsRecordFile) + "\n]";
            JSONArray jsonArray = new JSONArray(jsonStr);
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
