package com.example.onetoonevideocallsample;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class ApiClient {
    private static final String BASE_URL = "https://fefc886d-d832-4914-a230-a85be20cb9b9-00-3rmr3sqnunpfa.riker.replit.dev/";

    public static String fetchData(String endpoint) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
