package com.hemend.flutter.plugins.pedometer.libs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Listens for alerts about steps being detected.
 */
public class DataManager {
    static String dataFilePath;

    public DataManager(String filePath) {
        dataFilePath = filePath;
    }

    public boolean clearData() {
        File f = new File(dataFilePath);

        if (f.exists()) {
            return f.delete();
        }

        return true;
    }

    public JSONObject getData(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        return getData(formatter.format(date));
    }

    public JSONObject getData(String date) {
        ArrayList<JSONObject> jsonlist = getData();

        Iterator iterator = jsonlist.iterator();

        while (iterator.hasNext()) {
            try {
                JSONObject json = (JSONObject) iterator.next();
                if(json.getString("date").contains(date)) {
                    return json;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return new JSONObject();
    }

    public ArrayList<JSONObject> getData() {
        ArrayList<JSONObject> json = new ArrayList<JSONObject>();
        String line;
        JSONObject obj;

        try {
            File f = new File(dataFilePath);
            if (f.exists()) {
                try (BufferedReader fis = new BufferedReader(new FileReader(f))) {
                    while ((line = fis.readLine()) != null) {
                        obj = getJSONObject(line);

                        if (obj.has("date") && obj.has("step")) {
                            json.add(obj);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }

    public JSONObject write(long milliseconds, int step) {
        JSONObject res = null;
        ArrayList<JSONObject> jsonlist = getData();

        try (FileWriter writer = new FileWriter(dataFilePath, false)) {
            Date date = new Date(milliseconds);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            String dateStr = formatter.format(date);
//            String dateStr = "2020-10-" + String.valueOf(getNumberBetweenRange(13, 15));
//            step = getNumberBetweenRange(0, 10);

            Iterator iterator = jsonlist.iterator();

            String jsonString = "";
            boolean exists = false;

            while (iterator.hasNext()) {
                JSONObject json = (JSONObject) iterator.next();

                if(json.getString("date").contains(dateStr)) {
                    exists = true;
                    json.put("step", json.getInt("step") + step);
                    res = json;
                }

                jsonString += json.toString() + "\n";
            }

            if(!exists) {
                JSONObject json = new JSONObject();
                json.put("date", dateStr).put("step", step);
                res = json;
                jsonString += json.toString() + "\n";
            }

            try {
                writer.write(jsonString);
            } finally {
                writer.close();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return res;
        }

        return res;
    }

    private static JSONObject getJSONObject(String text) {
        JSONObject json = new JSONObject();

        try {
            json = new JSONObject(text);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static int getNumberBetweenRange(int min, int max) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }
}
