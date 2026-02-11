package com.rakshak.security.health;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rakshak.security.R;

public class HealthDashboardActivity extends AppCompatActivity {

    private TextView tvBattery, tvTemp, tvRam, tvStorage, tvScore;
    private ProgressBar scoreProgress;
    private Button btnScanHealth;

    private final Handler handler = new Handler();

    private int fakeProgress = 0;
    private boolean isScanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_dashboard);

        initViews();
        setupScanButton();
    }

    private void initViews() {
        tvBattery = findViewById(R.id.tvBattery);
        tvTemp = findViewById(R.id.tvTemp);
        tvRam = findViewById(R.id.tvRam);
        tvStorage = findViewById(R.id.tvStorage);
        tvScore = findViewById(R.id.tvScore);
        scoreProgress = findViewById(R.id.scoreProgress);
        btnScanHealth = findViewById(R.id.btnScanHealth);

        scoreProgress.setMax(100);
        scoreProgress.setProgress(0);
    }

    private void setupScanButton() {
        btnScanHealth.setOnClickListener(v -> startPremiumScan());
    }

    // ================= PREMIUM SCAN =================

    private void startPremiumScan() {

        if (isScanning) return;

        isScanning = true;
        fakeProgress = 0;

        btnScanHealth.setEnabled(false);
        btnScanHealth.setText("Scanning...");

        scoreProgress.setProgress(0);
        tvScore.setText("0");

        handler.post(scanRunnable);
    }

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {

            if (fakeProgress < 100) {

                fakeProgress += 4; // scanning speed

                animateProgress(fakeProgress);

                handler.postDelayed(this, 60);

            } else {
                finishScan();
            }
        }
    };

    // ================= FINISH SCAN =================

    private void finishScan() {

        int realScore = calculateRealHealthScore();

        animateProgress(realScore);
        updateScoreColor(realScore);

        btnScanHealth.setEnabled(true);
        btnScanHealth.setText("Scan Device Health");

        isScanning = false;
    }

    // ================= REAL HEALTH =================

    private int calculateRealHealthScore() {

        float battery = BatteryMonitor.getBatteryPercentage(this);
        float temp = BatteryMonitor.getBatteryTemperature(this);
        float ram = MemoryMonitor.getUsedRamPercentage(this);
        float storage = StorageMonitor.getUsedStoragePercentage();

        tvBattery.setText("Battery: " + String.format("%.1f", battery) + "%");
        tvTemp.setText("Temperature: " + String.format("%.1f", temp) + "°C");
        tvRam.setText("RAM Used: " + String.format("%.1f", ram) + "%");
        tvStorage.setText("Storage Used: " + String.format("%.1f", storage) + "%");

        return HealthScoreEngine.calculateScore(battery, temp, ram, storage);
    }

    // ================= PROGRESS ANIMATION =================

    private void animateProgress(int value) {

        ObjectAnimator animation = ObjectAnimator.ofInt(
                scoreProgress,
                "progress",
                scoreProgress.getProgress(),
                value
        );

        animation.setDuration(120);
        animation.start();

        tvScore.setText(String.valueOf(value));
    }

    // ================= COLOR LOGIC =================

    private void updateScoreColor(int score) {

        int color;

        if (score >= 80) {
            color = getColor(android.R.color.holo_green_light);
        } else if (score >= 50) {
            color = getColor(android.R.color.holo_orange_light);
        } else {
            color = getColor(android.R.color.holo_red_light);
        }

        scoreProgress.getProgressDrawable().setTint(color);
    }
}
