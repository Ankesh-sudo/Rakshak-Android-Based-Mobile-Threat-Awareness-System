package com.rakshak.security.calls;

import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

import com.rakshak.security.calls.engine.CallRiskResult;
import com.rakshak.security.calls.engine.CallThreatEngine;

public class RakshakCallScreeningService extends CallScreeningService {

    private static final String TAG = "RAKSHAK_CALL";
    public static final String ACTION_SHOW_WARNING =
            "com.rakshak.security.ACTION_SHOW_WARNING";

    @Override
    public void onScreenCall(Call.Details callDetails) {

        try {

            Log.d(TAG, "Incoming call detected");

            if (callDetails == null) {
                return;
            }

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
            // Evaluate Risk
            // ===============================
            CallRiskResult result =
                    CallThreatEngine.evaluate(this, number);

            Log.d(TAG, "Risk Score: " + result.getRiskScore());
            Log.d(TAG, "Threat Level: " + result.getThreatLevel());

            CallResponse.Builder response = new CallResponse.Builder();

            switch (result.getThreatLevel()) {

                case HIGH_RISK:
                case SPAM:

                    Log.d(TAG, "Blocking call");

                    response.setDisallowCall(true)
                            .setRejectCall(true)
                            .setSkipCallLog(false)
                            .setSkipNotification(false);
                    break;

                case SUSPICIOUS:

                    Log.d(TAG, "Suspicious call — sending broadcast");

                    response.setDisallowCall(false);
                    sendWarningBroadcast(number, result.getRiskScore());
                    break;

                case SAFE:
                default:

                    Log.d(TAG, "Safe call — allowing");

                    response.setDisallowCall(false);
                    break;
            }

            respondToCall(callDetails, response.build());

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

    /**
     * Instead of starting Activity directly,
     * send broadcast. Much safer.
     */
    private void sendWarningBroadcast(String number, int score) {

        Intent intent = new Intent(ACTION_SHOW_WARNING);
        intent.setPackage(getPackageName());
        intent.putExtra("number", number);
        intent.putExtra("score", score);

        sendBroadcast(intent);
    }
}
