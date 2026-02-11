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

            Log.d(TAG, "Incoming call detected");

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

            // ===============================
            // STEP 1 — Heuristic Evaluation
            // ===============================
            CallRiskResult heuristicResult =
                    CallThreatEngine.evaluate(this, number);

            int baseScore = heuristicResult.getRiskScore();

            Log.d(TAG, "Heuristic Score: " + baseScore);

            // ===============================
            // ALWAYS ALLOW CALL IMMEDIATELY
            // ===============================
            CallResponse.Builder response =
                    new CallResponse.Builder()
                            .setDisallowCall(false)
                            .setSkipCallLog(false)
                            .setSkipNotification(false);

            respondToCall(callDetails, response.build());

            // ===============================
            // STEP 2 — REAL FEATURE EXTRACTION
            // ===============================

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

            Log.d(TAG, "Features → Freq: " + callFrequency +
                    ", AvgDur: " + callDuration +
                    ", Night: " + nightCall +
                    ", Unknown: " + unknownNumber +
                    ", ShortCalls: " + shortCalls);

            // ===============================
            // STEP 3 — ML Prediction
            // ===============================
            CloudCallClassifier.predictCall(
                    callFrequency,
                    callDuration,
                    nightCall,
                    unknownNumber,
                    shortCalls,
                    new CloudCallClassifier.CallPredictionCallback() {

                        @Override
                        public void onResult(float probability) {

                            Log.d(TAG, "ML Probability: " + probability);

                            int finalScore = baseScore;

                            if (probability > 0.9f) {
                                finalScore += 60;
                            } else if (probability > 0.75f) {
                                finalScore += 40;
                            } else if (probability > 0.6f) {
                                finalScore += 20;
                            }

                            Log.d(TAG, "Final Score After ML: " + finalScore);

                            // ⚠️ Only show warning — NEVER BLOCK
                            if (finalScore >= 50) {
                                sendWarningBroadcast(number, finalScore);
                            }
                        }

                        @Override
                        public void onError(String error) {

                            Log.e(TAG, "ML Error: " + error);

                            // Fallback to heuristic only
                            if (baseScore >= 50) {
                                sendWarningBroadcast(number, baseScore);
                            }
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
