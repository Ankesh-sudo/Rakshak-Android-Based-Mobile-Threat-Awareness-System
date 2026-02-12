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

public class CloudLinkClassifier {

    private static final String TAG = "LINK_ML";

    // ⚠️ Replace with your laptop IP
    private static final String BASE_URL =
            "http://10.2.1.172:5000/predict/link";

    private static final OkHttpClient client =
            new OkHttpClient();

    public interface LinkPredictionCallback {
        void onResult(float probability);
        void onError(String error);
    }

    public static void predictLink(
            int urlLength,
            int nDots,
            int nHypens,
            int nUnderline,
            int nSlash,
            int nQuestion,
            int nEqual,
            int nAt,
            int nAnd,
            int nExclamation,
            int nSpace,
            int nTilde,
            int nComma,
            int nPlus,
            int nAsterisk,
            int nHashtag,
            int nDollar,
            int nPercent,
            int nRedirection,
            LinkPredictionCallback callback
    ) {

        try {

            JSONObject json = new JSONObject();
            json.put("url_length", urlLength);
            json.put("n_dots", nDots);
            json.put("n_hypens", nHypens);
            json.put("n_underline", nUnderline);
            json.put("n_slash", nSlash);
            json.put("n_questionmark", nQuestion);
            json.put("n_equal", nEqual);
            json.put("n_at", nAt);
            json.put("n_and", nAnd);
            json.put("n_exclamation", nExclamation);
            json.put("n_space", nSpace);
            json.put("n_tilde", nTilde);
            json.put("n_comma", nComma);
            json.put("n_plus", nPlus);
            json.put("n_asterisk", nAsterisk);
            json.put("n_hastag", nHashtag);
            json.put("n_dollar", nDollar);
            json.put("n_percent", nPercent);
            json.put("n_redirection", nRedirection);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json")
            );

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
                public void onResponse(Call call, Response response)
                        throws IOException {

                    if (!response.isSuccessful()) {
                        callback.onError("Server Error");
                        return;
                    }

                    try {
                        String resBody = response.body().string();
                        JSONObject result =
                                new JSONObject(resBody);

                        float probability =
                                (float) result.getDouble("probability");

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
