package com.rakshak.security.health;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.rakshak.security.R;

import java.util.ArrayList;

public class HealthDashboardActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 2001;

    private TextView tvBattery, tvTemp, tvRam, tvStorage, tvScore;
    private TextView tvScanStatus, tvScanTime;

    private ProgressBar scoreProgress;
    private Button btnScanHealth;
    private Button btnViewThreats;

    private final Handler handler = new Handler();

    private int fakeProgress = 0;
    private boolean isScanning = false;
    private long scanStartTime;

    // 🔴 Store last scan threats
    private ArrayList<MediaStoreScanner.ScanFile> lastThreats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_dashboard);

        initViews();
        setupButtons();
    }

    private void initViews() {

        tvBattery = findViewById(R.id.tvBattery);
        tvTemp = findViewById(R.id.tvTemp);
        tvRam = findViewById(R.id.tvRam);
        tvStorage = findViewById(R.id.tvStorage);
        tvScore = findViewById(R.id.tvScore);
        tvScanStatus = findViewById(R.id.tvScanStatus);
        tvScanTime = findViewById(R.id.tvScanTime);

        scoreProgress = findViewById(R.id.scoreProgress);

        btnScanHealth = findViewById(R.id.btnScanHealth);
        btnViewThreats = findViewById(R.id.btnViewThreats);

        scoreProgress.setMax(100);
        scoreProgress.setProgress(0);
    }

    private void setupButtons() {

        btnScanHealth.setOnClickListener(v -> checkPermissionAndStart());

        btnViewThreats.setOnClickListener(v -> openThreatScreen());
    }

    // ================= PERMISSION =================

    private void checkPermissionAndStart() {

        if (hasStoragePermission()) {
            startPremiumScan();
        } else {
            requestStoragePermission();
        }
    }

    private boolean hasStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;

        } else {

            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                    },
                    STORAGE_PERMISSION_CODE
            );

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {

            boolean granted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                startPremiumScan();
            }
        }
    }

    // ================= START SCAN =================

    private void startPremiumScan() {

        if (isScanning) return;

        isScanning = true;
        fakeProgress = 0;
        scanStartTime = System.currentTimeMillis();

        btnScanHealth.setEnabled(false);
        btnScanHealth.setText("Scanning...");

        tvScanStatus.setText("Initializing scan...");
        tvScanTime.setText("");

        scoreProgress.setProgress(0);
        tvScore.setText("0");

        handler.post(scanRunnable);
    }

    // ================= FAKE PROGRESS =================

    private final Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {

            if (fakeProgress < 100) {

                fakeProgress += 4;
                animateProgress(fakeProgress);

                handler.postDelayed(this, 60);

            } else {
                finishScan();
            }
        }
    };

    // ================= FINISH SCAN =================

    private void finishScan() {

        new Thread(() -> {

            MediaStoreScanner.ScanResult fileResult =
                    MediaStoreScanner.scanDevice(
                            this,
                            status -> runOnUiThread(() ->
                                    tvScanStatus.setText(status)
                            )
                    );

            int baseScore = calculateRealHealthScore();
            int finalScore = adjustScoreWithThreats(baseScore, fileResult);

            long scanDuration = System.currentTimeMillis() - scanStartTime;

            // 🔴 Save threats for manual viewing
            lastThreats = new ArrayList<>(fileResult.securityThreats);

            runOnUiThread(() -> {

                animateProgress(finalScore);
                updateScoreColor(finalScore);

                tvScanTime.setText("Scan Time: " + (scanDuration / 1000.0) + " sec");
                tvScanStatus.setText("Scan Completed");

                showFileScanSummary(fileResult);

                btnScanHealth.setEnabled(true);
                btnScanHealth.setText("Scan Device Health");

                isScanning = false;
            });

        }).start();
    }

    // ================= OPEN THREAT SCREEN =================

    private void openThreatScreen() {

        if (lastThreats == null || lastThreats.isEmpty()) {
            tvScanStatus.setText("No security threats detected 🎉");
            return;
        }

        Intent intent = new Intent(this, ThreatListActivity.class);
        intent.putExtra(
                ThreatListActivity.EXTRA_THREATS,
                lastThreats
        );
        startActivity(intent);
    }

    // ================= HEALTH SCORE =================

    private int calculateRealHealthScore() {

        float battery = BatteryMonitor.getBatteryPercentage(this);
        float temp = BatteryMonitor.getBatteryTemperature(this);
        float ram = MemoryMonitor.getUsedRamPercentage(this);
        float storage = StorageMonitor.getUsedStoragePercentage();

        runOnUiThread(() -> {
            tvBattery.setText("Battery: " + String.format("%.1f", battery) + "%");
            tvTemp.setText("Temperature: " + String.format("%.1f", temp) + "°C");
            tvRam.setText("RAM Used: " + String.format("%.1f", ram) + "%");
        });

        return HealthScoreEngine.calculateScore(battery, temp, ram, storage);
    }

    // ================= THREAT PENALTY =================

    private int adjustScoreWithThreats(int baseScore,
                                       MediaStoreScanner.ScanResult result) {

        int penalty = result.suspiciousFiles * 3;

        return Math.max(baseScore - penalty, 0);
    }

    // ================= SUMMARY =================

    private void showFileScanSummary(MediaStoreScanner.ScanResult result) {

        String summary =
                "Files Scanned: " + result.totalFiles + "\n" +
                        "Security Threats: " + result.suspiciousFiles + "\n" +
                        "Hidden Files: " + result.hiddenFiles + "\n" +
                        "Large Files (>100MB): " + result.largeFiles;

        tvStorage.setText(summary);
    }

    // ================= ANIMATION =================

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
