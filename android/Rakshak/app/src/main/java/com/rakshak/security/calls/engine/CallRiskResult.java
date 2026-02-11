package com.rakshak.security.calls.engine;

public class CallRiskResult {

    public enum ThreatLevel {
        SAFE,
        SUSPICIOUS,
        SPAM,
        HIGH_RISK
    }

    private final ThreatLevel threatLevel;
    private final int riskScore;

    public CallRiskResult(ThreatLevel level, int score) {
        this.threatLevel = level;
        this.riskScore = score;
    }

    public ThreatLevel getThreatLevel() {
        return threatLevel;
    }

    public int getRiskScore() {
        return riskScore;
    }
}
