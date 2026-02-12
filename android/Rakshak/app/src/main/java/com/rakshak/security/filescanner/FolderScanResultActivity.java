package com.rakshak.security.filescanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.MainActivity;
import com.rakshak.security.R;

public class FolderScanResultActivity extends AppCompatActivity {

    private TextView txtRiskLevel;
    private TextView txtRiskDescription;
    private LinearLayout riskCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_scan_result);

        txtRiskLevel = findViewById(R.id.txtRiskLevel);
        txtRiskDescription = findViewById(R.id.txtRiskDescription);
        riskCard = findViewById(R.id.riskCard);

        Button btnScanAnother = findViewById(R.id.btnScanAnother);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        // ===============================
        // SAFELY GET RESULT FROM INTENT
        // ===============================

        Intent intent = getIntent();

        if (intent == null || !intent.hasExtra("result")) {
            // No result passed → close screen silently
            finish();
            return;
        }

        String resultText = intent.getStringExtra("result");

        if (resultText == null || resultText.trim().isEmpty()) {
            finish();
            return;
        }

        // ===============================
        // PARSE THREAT LEVEL
        // ===============================

        resultText = resultText.trim().toUpperCase();

        if (resultText.startsWith("SAFE")) {
            txtRiskLevel.setText("SAFE");
            riskCard.setBackgroundResource(R.drawable.bg_risk_safe);

        } else if (resultText.startsWith("MALICIOUS")) {
            txtRiskLevel.setText("MALICIOUS");
            riskCard.setBackgroundResource(R.drawable.bg_risk_high);

        } else {
            txtRiskLevel.setText("SUSPICIOUS");
            riskCard.setBackgroundResource(R.drawable.bg_risk_caution);
        }

        txtRiskDescription.setText(resultText);

        // ===============================
        // BUTTON ACTIONS
        // ===============================

        btnScanAnother.setOnClickListener(v -> finish());

        btnBackHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(
                    FolderScanResultActivity.this,
                    MainActivity.class
            );

            homeIntent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(homeIntent);
            finish();
        });
    }
}
