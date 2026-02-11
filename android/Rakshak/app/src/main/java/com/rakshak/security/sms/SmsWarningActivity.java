package com.rakshak.security.sms;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.R;

public class SmsWarningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_warning);

        String sender = getIntent().getStringExtra("sender");
        String message = getIntent().getStringExtra("message");
        int score = getIntent().getIntExtra("score", 0);
        String level = getIntent().getStringExtra("level");

        TextView senderView = findViewById(R.id.senderText);
        TextView messageView = findViewById(R.id.messageText);
        TextView riskView = findViewById(R.id.riskText);

        senderView.setText("Sender: " + sender);
        messageView.setText("Message:\n" + message);
        riskView.setText("Risk Level: " + level + " (" + score + ")");
    }
}
