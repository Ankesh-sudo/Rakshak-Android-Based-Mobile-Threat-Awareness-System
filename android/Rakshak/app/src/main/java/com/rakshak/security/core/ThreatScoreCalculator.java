package com.rakshak.security.core;

public class ThreatScoreCalculator {

    public static String getRiskLevel(int score) {

        if (score < 20) return "LOW";
        if (score < 50) return "MEDIUM";
        if (score < 80) return "HIGH";
        return "CRITICAL";
    }
}
