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

        // Get result text from intent
        String resultText = getIntent().getStringExtra("result");

        if (resultText == null) {
            showError();
            return;
        }

        // Parse threat level from text
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

        // Actions
        btnScanAnother.setOnClickListener(v -> finish());

        btnBackHome.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            FolderScanResultActivity.this,
                            MainActivity.class
                    );
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );
            startActivity(intent);
            finish();
        });
    }

    private void showError() {
        txtRiskLevel.setText("ERROR");
        txtRiskDescription.setText("Unable to display scan result.");
        riskCard.setBackgroundResource(R.drawable.bg_risk_caution);
    }
}
