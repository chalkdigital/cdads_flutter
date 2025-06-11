package com.chalkdigital.ads.resource;

import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class SparkJavascript {
    public static String JAVASCRIPT_SOURCE = null;

    public static String getJavascriptSource (Context context) {
        if (JAVASCRIPT_SOURCE!=null)
            return JAVASCRIPT_SOURCE;
        try {
            InputStream inputStream = context.getAssets().open("holaPlayer.js");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            List<String> lines = new LinkedList<>();
            String line = bufferedReader.readLine();
            while(line != null) {
                lines.add(line);
                line = bufferedReader.readLine();
            }
            String separator = System.getProperty("line.separator");
            JAVASCRIPT_SOURCE = TextUtils.join(separator, lines);
            return JAVASCRIPT_SOURCE;
        } catch (IOException e) {
            return "";
        }
    }
}
