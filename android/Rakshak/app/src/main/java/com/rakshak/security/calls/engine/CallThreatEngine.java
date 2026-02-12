package com.rakshak.security.calls.engine;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.rakshak.security.calls.engine.detectors.ContactDetector;
import com.rakshak.security.calls.engine.detectors.CountryCodeDetector;
import com.rakshak.security.calls.engine.detectors.FrequencyDetector;
import com.rakshak.security.calls.engine.detectors.PatternDetector;
import com.rakshak.security.calls.engine.detectors.ReputationDetector;

public class CallThreatEngine {

    private static final String TAG = "RAKSHAK_ENGINE";

    public static CallRiskResult evaluate(Context context, String number) {

        try {

            if (TextUtils.isEmpty(number)) {
                Log.d(TAG, "Empty number received — default suspicious score");
                return new CallRiskResult(
                        CallRiskResult.ThreatLevel.SUSPICIOUS,
                        40
                );
            }

            // Normalize number
            number = number.replaceAll("[^+0-9]", "");
            Log.d(TAG, "Evaluating number: " + number);

            CallRiskModel model = new CallRiskModel();

            // ===============================
            // SAFE DETECTOR EXECUTION
            // ===============================

            try {
                model.patternScore = PatternDetector.analyze(number);
                Log.d(TAG, "Pattern Score: " + model.patternScore);
            } catch (Exception e) {
                Log.e(TAG, "PatternDetector crash", e);
            }

            try {
                model.frequencyScore = FrequencyDetector.analyze(context, number);
                Log.d(TAG, "Frequency Score: " + model.frequencyScore);
            } catch (Exception e) {
                Log.e(TAG, "FrequencyDetector crash", e);
            }

            try {
                model.countryScore = CountryCodeDetector.analyze(number);
                Log.d(TAG, "Country Score: " + model.countryScore);
            } catch (Exception e) {
                Log.e(TAG, "CountryCodeDetector crash", e);
            }

            try {
                model.reputationScore = ReputationDetector.analyze(number);
                Log.d(TAG, "Reputation Score: " + model.reputationScore);
            } catch (Exception e) {
                Log.e(TAG, "ReputationDetector crash", e);
            }

            try {
                model.contactScore = ContactDetector.analyze(context, number);
                Log.d(TAG, "Contact Score: " + model.contactScore);
            } catch (Exception e) {
                Log.e(TAG, "ContactDetector crash", e);
            }

            int totalScore = model.getTotalScore();
            totalScore = clamp(totalScore);

            Log.d(TAG, "Final Engine Score: " + totalScore);

            // ===============================
            // DECISION (NO MODE LOGIC HERE)
            // ===============================

            CallRiskResult.ThreatLevel level = decide(totalScore);

            Log.d(TAG, "Final Threat Level: " + level.name());

            return new CallRiskResult(level, totalScore);

        } catch (Exception e) {

            Log.e(TAG, "Critical engine failure", e);

            return new CallRiskResult(
                    CallRiskResult.ThreatLevel.SAFE,
                    0
            );
        }
    }

    // ===============================
    // Decision Helper
    // ===============================

    private static CallRiskResult.ThreatLevel decide(int score) {

        if (score >= 80)
            return CallRiskResult.ThreatLevel.HIGH_RISK;

        if (score >= 60)
            return CallRiskResult.ThreatLevel.SPAM;

        if (score >= 40)
            return CallRiskResult.ThreatLevel.SUSPICIOUS;

        return CallRiskResult.ThreatLevel.SAFE;
    }

    private static int clamp(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }
}
