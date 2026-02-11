package com.rakshak.security.core;

import java.util.List;

public class ThreatResult {

    private int score;
    private String riskLevel;
    private List<String> reasons;

    public ThreatResult(int score, String riskLevel, List<String> reasons) {
        this.score = score;
        this.riskLevel = riskLevel;
        this.reasons = reasons;
    }

    public int getScore() { return score; }
    public String getRiskLevel() { return riskLevel; }
    public List<String> getReasons() { return reasons; }
}
