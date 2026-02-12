package com.rakshak.security.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.R;
import com.rakshak.security.calls.CallProtectionMode;
import com.rakshak.security.calls.CallSettingsManager;

public class CallProtectionSettingsActivity extends AppCompatActivity {

    private Button basicBtn, smartBtn;
    private CallSettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_protection_settings);

        basicBtn = findViewById(R.id.btnBasic);
        smartBtn = findViewById(R.id.btnSmart);

        settingsManager = new CallSettingsManager(this);

        basicBtn.setOnClickListener(v -> {

            settingsManager.setMode(CallProtectionMode.ALL_CALLS_ALLOWED);

            Toast.makeText(this,
                    "Basic Protection Enabled",
                    Toast.LENGTH_SHORT).show();

            finish();
        });

        smartBtn.setOnClickListener(v -> {

            settingsManager.setMode(CallProtectionMode.ALLOW_KNOWN_ONLY);

            Toast.makeText(this,
                    "Smart Protection Enabled",
                    Toast.LENGTH_SHORT).show();

            finish();
        });
    }
}
