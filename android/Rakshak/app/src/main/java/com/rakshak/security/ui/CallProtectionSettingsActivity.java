package com.rakshak.security.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.R;
import com.rakshak.security.calls.ProtectionModeManager;

public class CallProtectionSettingsActivity extends AppCompatActivity {

    Button basicBtn, smartBtn, extremeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_protection_settings);

        basicBtn = findViewById(R.id.btnBasic);
        smartBtn = findViewById(R.id.btnSmart);
        extremeBtn = findViewById(R.id.btnExtreme);

        basicBtn.setOnClickListener(v -> {
            ProtectionModeManager.setMode(
                    this,
                    ProtectionModeManager.ProtectionMode.BASIC
            );
            Toast.makeText(this, "Basic Protection Enabled", Toast.LENGTH_SHORT).show();
            finish();
        });

        smartBtn.setOnClickListener(v -> {
            ProtectionModeManager.setMode(
                    this,
                    ProtectionModeManager.ProtectionMode.SMART
            );
            Toast.makeText(this, "Smart Protection Enabled", Toast.LENGTH_SHORT).show();
            finish();
        });

        extremeBtn.setOnClickListener(v -> {
            ProtectionModeManager.setMode(
                    this,
                    ProtectionModeManager.ProtectionMode.EXTREME
            );
            Toast.makeText(this, "Extreme Protection Enabled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
