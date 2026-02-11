package com.rakshak.security.health;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.rakshak.security.R;

public class HealthDashboardActivity extends AppCompatActivity {

    private TextView tvBattery, tvTemp, tvRam, tvStorage, tvScore;
    private ProgressBar scoreProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_dashboard);

        initViews();
        loadHealthData();
    }

    private void initViews() {
        tvBattery = findViewById(R.id.tvBattery);
        tvTemp = findViewById(R.id.tvTemp);
        tvRam = findViewById(R.id.tvRam);
        tvStorage = findViewById(R.id.tvStorage);
        tvScore = findViewById(R.id.tvScore);
        scoreProgress = findViewById(R.id.scoreProgress);
    }

    private void loadHealthData() {

        float battery = BatteryMonitor.getBatteryPercentage(this);
        float temp = BatteryMonitor.getBatteryTemperature(this);
        float ram = MemoryMonitor.getUsedRamPercentage(this);
        float storage = StorageMonitor.getUsedStoragePercentage();

        int score = HealthScoreEngine.calculateScore(battery, temp, ram, storage);

        tvBattery.setText("Battery: " + String.format("%.1f", battery) + "%");
        tvTemp.setText("Temperature: " + String.format("%.1f", temp) + "°C");
        tvRam.setText("RAM Used: " + String.format("%.1f", ram) + "%");
        tvStorage.setText("Storage Used: " + String.format("%.1f", storage) + "%");

        tvScore.setText("Health Score: " + score);
        scoreProgress.setProgress(score);
    }
}
