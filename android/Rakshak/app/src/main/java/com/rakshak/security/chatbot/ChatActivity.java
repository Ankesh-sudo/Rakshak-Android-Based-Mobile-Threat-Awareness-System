package com.rakshak.security.chatbot;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rakshak.security.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;

    private EditText inputMessage;
    private ImageButton sendButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewChat);
        inputMessage = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Welcome message
        addBotMessage("Hello 👋 I am Rakshak AI. How can I help you with cybersecurity today?");

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {

        String userText = inputMessage.getText().toString().trim();

        if (userText.isEmpty()) return;

        addUserMessage(userText);
        inputMessage.setText("");

        // Call ChatService
        ChatService.sendMessage(userText, new ChatService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> addBotMessage(response));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        addBotMessage("⚠️ " + errorMessage));
            }
        });
    }

    private void addUserMessage(String message) {
        messageList.add(new ChatMessage(message, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String message) {
        messageList.add(new ChatMessage(message, false));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
}
