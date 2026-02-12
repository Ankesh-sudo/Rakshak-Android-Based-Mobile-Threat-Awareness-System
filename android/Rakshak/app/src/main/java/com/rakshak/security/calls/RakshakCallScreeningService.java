package com.rakshak.security.calls;

import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import com.rakshak.security.calls.engine.CallFeatureExtractor;
import com.rakshak.security.calls.engine.CallRiskResult;
import com.rakshak.security.calls.engine.CallThreatEngine;
import com.rakshak.security.core.ml.CloudCallClassifier;

public class RakshakCallScreeningService extends CallScreeningService {

    private static final String TAG = "RAKSHAK_CALL";
    public static final String ACTION_SHOW_WARNING =
            "com.rakshak.security.ACTION_SHOW_WARNING";

    @Override
    public void onScreenCall(Call.Details callDetails) {

        try {

            if (callDetails == null) return;

            Uri handle = callDetails.getHandle();
            if (handle == null) {
                allowCall(callDetails);
                return;
            }

            String number = handle.getSchemeSpecificPart();
            if (number == null || number.trim().isEmpty()) {
                allowCall(callDetails);
                return;
            }

            Log.d(TAG, "Incoming Number: " + number);

            // STEP 1 — Heuristic
            CallRiskResult heuristicResult =
                    CallThreatEngine.evaluate(this, number);

            int baseScore = heuristicResult.getRiskScore();

            // STEP 2 — Feature Extraction
            int callFrequency =
                    CallFeatureExtractor.getCallFrequency(this, number);

            int callDuration =
                    CallFeatureExtractor.getAverageCallDuration(this, number);

            int nightCall =
                    CallFeatureExtractor.isNightCall();

            int unknownNumber =
                    CallFeatureExtractor.isUnknownNumber(this, number);

            int shortCalls =
                    CallFeatureExtractor.getShortCallCount(this, number);

            // STEP 3 — ML Prediction
            CloudCallClassifier.predictCall(
                    callFrequency,
                    callDuration,
                    nightCall,
                    unknownNumber,
                    shortCalls,
                    new CloudCallClassifier.CallPredictionCallback() {

                        @Override
                        public void onResult(float probability) {

                            int finalScore =
                                    calculateFinalScore(baseScore, probability);

                            handleFinalDecision(
                                    callDetails,
                                    number,
                                    finalScore,
                                    unknownNumber == 1
                            );
                        }

                        @Override
                        public void onError(String error) {

                            handleFinalDecision(
                                    callDetails,
                                    number,
                                    baseScore,
                                    unknownNumber == 1
                            );
                        }
                    }
            );

        } catch (Exception e) {

            Log.e(TAG, "Screening error: ", e);

            if (callDetails != null) {
                allowCall(callDetails);
            }
        }
    }

    // =====================================================
    // SCORE CALCULATION
    // =====================================================

    private int calculateFinalScore(int baseScore, float probability) {

        int score = baseScore;

        if (probability > 0.9f) {
            score += 60;
        } else if (probability > 0.75f) {
            score += 40;
        } else if (probability > 0.6f) {
            score += 20;
        }

        return score;
    }

    // =====================================================
    // FINAL DECISION ENGINE
    // =====================================================

    private void handleFinalDecision(Call.Details callDetails,
                                     String number,
                                     int score,
                                     boolean isUnknown) {

        CallSettingsManager settings =
                new CallSettingsManager(getApplicationContext());

        CallProtectionMode mode = settings.getMode();

        boolean isSpam = score >= 50;
        boolean isSavedContact = !isUnknown;

        boolean shouldBlock = false;

        switch (mode) {

            case ALL_CALLS_ALLOWED:
                // Basic Mode → Allow everything
                shouldBlock = false;
                break;

            case ALLOW_KNOWN_ONLY:
                // Smart Mode → Allow only saved contacts
                // Block unsaved OR spam
                if (!isSavedContact || isSpam) {
                    shouldBlock = true;
                }
                break;
        }

        CallResponse.Builder builder = new CallResponse.Builder();

        if (shouldBlock) {

            Log.d(TAG, "CALL BLOCKED | Mode: " + mode.name());

            builder.setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(false);

        } else {

            Log.d(TAG, "CALL ALLOWED | Mode: " + mode.name());

            builder.setDisallowCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false);
        }

        respondToCall(callDetails, builder.build());

        // Show warning if spam
        if (isSpam) {
            sendWarningBroadcast(number, score);
        }
    }

    private void allowCall(Call.Details callDetails) {
        respondToCall(callDetails,
                new CallResponse.Builder()
                        .setDisallowCall(false)
                        .build());
    }

    private void sendWarningBroadcast(String number, int score) {

        Intent intent = new Intent(ACTION_SHOW_WARNING);
        intent.setPackage(getPackageName());
        intent.putExtra("number", number);
        intent.putExtra("score", score);

        sendBroadcast(intent);
    }
}
