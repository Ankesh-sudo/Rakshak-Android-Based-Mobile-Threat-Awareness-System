package com.rakshak.security.calls.engine;

public class ThreatDecisionEngine {

    public static CallRiskResult.ThreatLevel decide(int score) {

        if (score >= 80)
            return CallRiskResult.ThreatLevel.HIGH_RISK;

        if (score >= 60)
            return CallRiskResult.ThreatLevel.SPAM;

        if (score >= 40)
            return CallRiskResult.ThreatLevel.SUSPICIOUS;

        return CallRiskResult.ThreatLevel.SAFE;
    }
}
