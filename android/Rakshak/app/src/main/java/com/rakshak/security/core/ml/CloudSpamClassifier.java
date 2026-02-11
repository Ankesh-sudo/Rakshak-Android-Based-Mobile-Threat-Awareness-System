package com.rakshak.security.core.ml;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudSpamClassifier {

    private static final String API_URL =
            "http://10.2.1.172:5000/predict"; // Your Flask IP

    public interface MLCallback {
        void onResult(float probability);
    }

    public static void checkSpam(String message, MLCallback callback) {

        OkHttpClient client = new OkHttpClient();

        try {

            JSONObject json = new JSONObject();
            json.put("message", message);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ML_ERROR", e.getMessage());
                    callback.onResult(0f);
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {

                    String responseData = response.body().string();

                    try {
                        JSONObject obj = new JSONObject(responseData);
                        float prob =
                                (float) obj.getDouble("spam_probability");

                        Log.d("ML_RESPONSE",
                                "Spam Probability: " + prob);

                        callback.onResult(prob);

                    } catch (Exception e) {
                        Log.e("ML_PARSE_ERROR", e.getMessage());
                        callback.onResult(0f);
                    }
                }
            });

        } catch (Exception e) {
            Log.e("ML_EXCEPTION", e.getMessage());
            callback.onResult(0f);
        }
    }
}
