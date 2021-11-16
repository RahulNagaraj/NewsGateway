package com.example.newsgateway;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class NewsAPIRunnable implements Runnable {
    private static final String TAG = "NewsAPIRunnable";
    private MainActivity mainActivity;
    private static final String API_KEY = "747532a529ca4fe69a820b3039e62438";
    // https://newsapi.org/v2/sources?apiKey=________
    private static final String NEWS_API_SOURCES_URI = "https://newsapi.org/v2/sources";
    // https://newsapi.org/v2/top-headlines?sources=______&apiKey=______
    private static final String NEWS_API_ARTICLES_URI = "https://newsapi.org/v2/top-headlines";

    public NewsAPIRunnable() {}

    public NewsAPIRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(NEWS_API_SOURCES_URI).buildUpon();
        buildURL.appendQueryParameter("apiKey", API_KEY);

        String urlToUse = buildURL.build().toString();
        StringBuilder sb = new StringBuilder();

        Log.d(TAG, "run: URL " + urlToUse);

        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent", "");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTPS ResponseCode NOT OK: " + conn.getResponseCode() + " , " + conn.getResponseMessage());
                handleResults(null);
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            reader.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        handleResults(sb.toString());
    }

    private void handleResults(String s) {

        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        List<Source> sourcesList = parseJSON(s);
        mainActivity.runOnUiThread(() -> {
            if (sourcesList != null || sourcesList.size() > 0) {
                mainActivity.updateData(sourcesList);
            }
        });
    }

    private List<Source> parseJSON(String s) {
        ArrayList<Source> sources = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("sources");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject source = (JSONObject) jsonArray.get(i);
                Source src = new Source(
                        source.getString("id"),
                        source.getString("name"),
                        source.getString("description"),
                        source.getString("url"),
                        source.getString("category"),
                        source.getString("language"),
                        source.getString("country")
                );
                sources.add(src);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sources;
    }
}
