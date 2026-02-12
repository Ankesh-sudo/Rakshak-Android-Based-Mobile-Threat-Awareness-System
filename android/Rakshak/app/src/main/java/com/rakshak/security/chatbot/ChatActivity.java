package com.rakshak.security.chatbot;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.rakshak.security.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;

    private EditText inputMessage;
    private MaterialButton sendButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerViewChat);
        inputMessage = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);

        // Setup toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbarChat);
        toolbar.setNavigationOnClickListener(v -> finish());

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        addBotMessage("Hello 👋 I am Rakshak AI. How can I help you with cybersecurity today?");

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {

        String userText = inputMessage.getText().toString().trim();

        if (userText.isEmpty()) return;

        addUserMessage(userText);
        inputMessage.setText("");

        try {

            // TEMP SAFE RESPONSE (remove ChatService temporarily)
            simulateAIResponse(userText);

        } catch (Exception e) {
            addBotMessage("⚠️ AI service unavailable.");
        }
    }

    // Simulated AI response to avoid crash
    private void simulateAIResponse(String userText) {

        recyclerView.postDelayed(() -> {
            addBotMessage("🔐 Rakshak AI Response: I analyzed your query about \"" +
                    userText + "\" and it appears safe.");
        }, 800);
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
