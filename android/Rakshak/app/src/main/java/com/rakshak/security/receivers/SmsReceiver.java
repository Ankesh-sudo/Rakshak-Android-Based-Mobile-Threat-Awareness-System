package com.rakshak.security.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.rakshak.security.core.RiskEngine;
import com.rakshak.security.core.RiskResult;
import com.rakshak.security.core.database.RiskDatabase;
import com.rakshak.security.core.database.RiskEntity;
import com.rakshak.security.core.ml.CloudSpamClassifier;
import com.rakshak.security.utils.NotificationUtil;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (context == null || intent == null) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        String format = bundle.getString("format");

        if (pdus == null) return;

        for (Object pdu : pdus) {

            SmsMessage message;

            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    message = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    message = SmsMessage.createFromPdu((byte[]) pdu);
                }
            } catch (Exception e) {
                continue;
            }

            if (message == null) continue;

            String sender = message.getOriginatingAddress();
            String body = message.getMessageBody();

            if (body == null) continue;

            final String finalSender = sender != null ? sender : "Unknown";
            final String finalBody = body;
            final Context finalContext = context;

            // ===============================
            // STEP 1: Heuristic Analysis
            // ===============================
            RiskResult heuristicResult =
                    RiskEngine.analyzeIncomingSms(finalSender, finalBody);

            final int baseScore = heuristicResult.getScore();

            // ===============================
            // STEP 2: ML Analysis (Async)
            // ===============================
            CloudSpamClassifier.checkSpam(finalBody, probability -> {

                Log.d("ML_RESPONSE", "Spam Probability: " + probability);

                int finalScore = baseScore;

                // Merge ML probability into risk score
                if (probability > 0.90f) {
                    finalScore += 60;
                } else if (probability > 0.75f) {
                    finalScore += 40;
                } else if (probability > 0.60f) {
                    finalScore += 20;
                }

                // Recalculate final risk using updated score logic
                RiskResult finalResult =
                        RiskEngine.analyzeIncomingSms(finalSender, finalBody);

                final int scoreToSave = finalScore;

                // ===============================
                // Save to Database (Background)
                // ===============================
                new Thread(() -> {

                    RiskEntity entity = new RiskEntity(
                            "SMS",
                            finalSender,
                            scoreToSave,
                            finalResult.getRiskLevel().name(),
                            System.currentTimeMillis(),
                            finalBody // ✅ Store full SMS message
                    );

                    RiskDatabase.getInstance(finalContext)
                            .riskDao()
                            .insert(entity);

                }).start();

                // ===============================
                // Show Warning if Needed
                // ===============================
                if (finalResult.shouldWarnUser()) {

                    NotificationUtil.showSmsWarning(
                            finalContext,
                            finalSender,
                            finalBody,
                            finalResult
                    );
                }
            });
        }
    }
}
