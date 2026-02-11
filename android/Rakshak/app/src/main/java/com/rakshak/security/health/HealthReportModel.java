package com.rakshak.security.health;

public class HealthReportModel {

    public float batteryPercent;
    public float temperature;
    public float ramUsedPercent;
    public float storageUsedPercent;
    public int healthScore;

    public HealthReportModel(float batteryPercent,
                             float temperature,
                             float ramUsedPercent,
                             float storageUsedPercent,
                             int healthScore) {

        this.batteryPercent = batteryPercent;
        this.temperature = temperature;
        this.ramUsedPercent = ramUsedPercent;
        this.storageUsedPercent = storageUsedPercent;
        this.healthScore = healthScore;
    }
}
