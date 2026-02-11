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

public class CloudCallClassifier {

    private static final String BASE_URL = "http://10.2.1.172:5000/predict/call";
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    public interface CallPredictionCallback {
        void onResult(float probability);
        void onError(String error);
    }

    public static void predictCall(
            int callFrequency,
            int callDuration,
            int nightCall,
            int unknownNumber,
            int shortCalls,
            CallPredictionCallback callback
    ) {

        try {
            JSONObject json = new JSONObject();
            json.put("call_frequency", callFrequency);
            json.put("call_duration", callDuration);
            json.put("night_call", nightCall);
            json.put("unknown_number", unknownNumber);
            json.put("short_calls", shortCalls);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("Server error");
                        return;
                    }

                    String responseBody = response.body().string();

                    try {
                        JSONObject res = new JSONObject(responseBody);
                        float probability = (float) res.getDouble("probability");
                        callback.onResult(probability);
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
