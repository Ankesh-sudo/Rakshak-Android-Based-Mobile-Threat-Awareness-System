package com.rakshak.security.core.ml;

import android.os.Handler;
import android.os.Looper;
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

public class CloudFileClassifier {

    private static final String TAG = "CLOUD_FILE";
    private static final String BASE_URL = "http://192.168.1.XXX:5000/predict/file";
    // ⚠ Replace with your laptop IP

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient client = new OkHttpClient();

    public interface FilePredictionCallback {
        void onResult(float probability);
        void onError(String error);
    }

    public static void predictFile(
            int fileSize,
            double entropy,
            int executable,
            int suspiciousExt,
            int hidden,
            int doubleExtension,
            int suspiciousName,
            FilePredictionCallback callback
    ) {

        try {

            JSONObject json = new JSONObject();
            json.put("file_size", fileSize);
            json.put("entropy", entropy);
            json.put("executable", executable);
            json.put("suspicious_ext", suspiciousExt);
            json.put("hidden", hidden);
            json.put("double_extension", doubleExtension);
            json.put("suspicious_name", suspiciousName);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    JSON
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    new Handler(Looper.getMainLooper())
                            .post(() -> callback.onError(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {

                    if (!response.isSuccessful()) {
                        callback.onError("Server error");
                        return;
                    }

                    String res = response.body().string();
                    try {

                        JSONObject obj = new JSONObject(res);
                        float probability =
                                (float) obj.getDouble("probability");

                        new Handler(Looper.getMainLooper())
                                .post(() -> callback.onResult(probability));

                    } catch (Exception e) {
                        callback.onError("Parse error");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
