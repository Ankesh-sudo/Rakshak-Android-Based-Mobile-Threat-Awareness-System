package com.rakshak.security.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.R;
import com.rakshak.security.core.RiskEngine;
import com.rakshak.security.core.SecurityRiskResult;

public class SecurityDashboardActivity extends AppCompatActivity {

    private ProgressBar riskProgress;
    private TextView scoreText;
    private TextView levelText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_dashboard);

        riskProgress = findViewById(R.id.riskProgress);
        scoreText = findViewById(R.id.scoreText);
        levelText = findViewById(R.id.levelText);

        updateDashboard();
    }

    private void updateDashboard() {

        SecurityRiskResult result =
                RiskEngine.evaluateOverallRisk(this, null);

        int score = result.getTotalScore();
        String level = result.getThreatLevel().name();

        riskProgress.setProgress(score);
        scoreText.setText(String.valueOf(score));
        levelText.setText(level);

        switch (result.getThreatLevel()) {

            case SAFE:
                levelText.setTextColor(Color.parseColor("#4CAF50"));
                break;

            case LOW:
                levelText.setTextColor(Color.parseColor("#8BC34A"));
                break;

            case MODERATE:
                levelText.setTextColor(Color.parseColor("#FFC107"));
                break;

            case HIGH:
                levelText.setTextColor(Color.parseColor("#FF5722"));
                break;

            case CRITICAL:
                levelText.setTextColor(Color.parseColor("#F44336"));
                break;
        }
    }
}
