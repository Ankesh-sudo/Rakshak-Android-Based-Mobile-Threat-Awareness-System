package com.rakshak.security.calls.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CallWarningActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);

        String number = getIntent().getStringExtra("number");
        int score = getIntent().getIntExtra("score", 0);

        textView.setText("⚠ Suspicious Call\n\nNumber: " +
                number + "\nRisk Score: " + score);

        textView.setTextSize(20f);
        setContentView(textView);
    }
}
