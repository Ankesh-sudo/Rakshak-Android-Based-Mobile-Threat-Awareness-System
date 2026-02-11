package com.rakshak.security.calls.engine;

public class CallRiskModel {

    public int patternScore;
    public int frequencyScore;
    public int countryScore;
    public int reputationScore;
    public int contactScore;     // NEW - unknown contact risk

    public int getTotalScore() {
        return patternScore
                + frequencyScore
                + countryScore
                + reputationScore
                + contactScore;
    }

    // Optional: Debug helper (very useful for logging later)
    @Override
    public String toString() {
        return "CallRiskModel{" +
                "patternScore=" + patternScore +
                ", frequencyScore=" + frequencyScore +
                ", countryScore=" + countryScore +
                ", reputationScore=" + reputationScore +
                ", contactScore=" + contactScore +
                '}';
    }
}
