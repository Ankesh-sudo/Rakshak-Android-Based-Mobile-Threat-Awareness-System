package com.rakshak.security.chatbot;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.rakshak.security.BuildConfig;

public class ChatService {

    private static final String TAG = "RakshakChatService";
    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");

    private static final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();

    // Callback interface for activity
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    // Public method to send message to Groq
    public static void sendMessage(String userMessage, ChatCallback callback) {

        try {

            String systemPrompt =
                    "You are Rakshak AI, a cybersecurity and fraud prevention assistant " +
                            "for Indian users. Only discuss cybersecurity, online scams, digital safety, " +
                            "and fraud prevention. Provide practical steps and keep responses under 300 words.";

            JSONObject requestJson = new JSONObject();

            requestJson.put("model", "llama-3.1-8b-instant");
            requestJson.put("max_tokens", 500);
            requestJson.put("temperature", 0.3);

            JSONArray messagesArray = new JSONArray();

            JSONObject systemObj = new JSONObject();
            systemObj.put("role", "system");
            systemObj.put("content", systemPrompt);

            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");
            userObj.put("content", userMessage);

            messagesArray.put(systemObj);
            messagesArray.put(userObj);

            requestJson.put("messages", messagesArray);

            RequestBody body =
                    RequestBody.create(requestJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(GROQ_API_URL)
                    .addHeader("Authorization",
                            "Bearer " + BuildConfig.GROQ_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Network error: ", e);
                    callback.onError("Network error. Please check connection.");
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API Error Code: " + response.code());
                        callback.onError("Server error: " + response.code());
                        return;
                    }

                    String responseBody = response.body().string();

                    try {

                        JSONObject jsonResponse =
                                new JSONObject(responseBody);

                        JSONArray choices =
                                jsonResponse.getJSONArray("choices");

                        if (choices.length() == 0) {
                            callback.onError("Empty response from AI.");
                            return;
                        }

                        JSONObject messageObject =
                                choices.getJSONObject(0)
                                        .getJSONObject("message");

                        String content =
                                messageObject.getString("content");

                        callback.onSuccess(content.trim());

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing error: ", e);
                        callback.onError("Error parsing AI response.");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: ", e);
            callback.onError("Unexpected error occurred.");
        }
    }
}
