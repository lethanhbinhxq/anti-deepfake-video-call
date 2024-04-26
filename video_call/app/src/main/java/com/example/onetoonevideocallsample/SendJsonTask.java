package com.example.onetoonevideocallsample;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendJsonTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "SendJsonTask";

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String jsonData = params[1];
        String response = "";

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(jsonData.getBytes());
            outputStream.flush();
            outputStream.close();

            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            inputStream.close();

            response = stringBuilder.toString();

            urlConnection.disconnect();
        } catch (IOException e) {
            Log.e(TAG, "Error sending JSON data", e);
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.d(TAG, "Server response: " + result);
    }
}
