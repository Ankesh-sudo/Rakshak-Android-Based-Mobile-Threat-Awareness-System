package com.rakshak.security.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.rakshak.security.core.RiskEngine;
import com.rakshak.security.core.RiskResult;
import com.rakshak.security.ui.OverlayWarningService;
import com.rakshak.security.utils.ContactUtils;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "Rakshak-CallReceiver";

    // Lightweight local call history (privacy-safe)
    private static final String PREFS_NAME = "rakshak_call_history";
    private static final String KEY_CALL_COUNT = "_count";
    private static final String KEY_LAST_START = "_start";
    private static final String KEY_LAST_DURATION = "_duration";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (context == null || intent == null) return;

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber =
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (state == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String safeNumber = normalizeNumber(incomingNumber);

        // ================= INCOMING CALL =================
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {

            Log.d(TAG, "📞 Incoming call ringing");

            boolean isInContacts =
                    ContactUtils.isNumberInContacts(context, safeNumber);

            int callFrequency =
                    prefs.getInt(safeNumber + KEY_CALL_COUNT, 0);

            long lastCallDuration =
                    prefs.getLong(safeNumber + KEY_LAST_DURATION, 0);

            Log.d(TAG, "📱 Number: " + safeNumber);
            Log.d(TAG, "📇 In contacts: " + isInContacts);
            Log.d(TAG, "🔁 Call frequency: " + callFrequency);
            Log.d(TAG, "⏱ Last call duration: " + lastCallDuration + "s");

            // -------- RISK ANALYSIS --------
            RiskResult result = RiskEngine.analyzeIncomingCall(
                    safeNumber,
                    isInContacts,
                    callFrequency,
                    lastCallDuration
            );

            Log.d(TAG, "🧠 Risk Score: " + result.score);
            Log.d(TAG, "🚦 Risk Level: " + result.level);

            // Build explanation text safely (no private access)
            StringBuilder reasonText = new StringBuilder();
            for (String reason : result.getReasons()) {
                reasonText.append("• ").append(reason).append("\n");
            }

            // -------- SHOW OVERLAY (MEDIUM / HIGH ONLY) --------
            if (result.level == RiskResult.RiskLevel.MEDIUM
                    || result.level == RiskResult.RiskLevel.HIGH) {

                Intent overlayIntent =
                        new Intent(context, OverlayWarningService.class);

                overlayIntent.putExtra("risk_level", result.level.name());
                overlayIntent.putExtra("risk_score", result.score);
                overlayIntent.putExtra(
                        "risk_reasons",
                        reasonText.toString().trim()
                );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(overlayIntent);
                } else {
                    context.startService(overlayIntent);
                }
            }
        }

        // ================= CALL ANSWERED =================
        else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {

            Log.d(TAG, "✅ Call answered");

            if (!TextUtils.isEmpty(safeNumber)) {
                prefs.edit()
                        .putLong(
                                safeNumber + KEY_LAST_START,
                                System.currentTimeMillis()
                        )
                        .apply();
            }
        }

        // ================= CALL ENDED =================
        else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {

            Log.d(TAG, "📴 Call ended");

            if (!TextUtils.isEmpty(safeNumber)) {

                long start =
                        prefs.getLong(safeNumber + KEY_LAST_START, 0);

                long durationSeconds = 0;
                if (start > 0) {
                    durationSeconds =
                            (System.currentTimeMillis() - start) / 1000;
                }

                int count =
                        prefs.getInt(safeNumber + KEY_CALL_COUNT, 0) + 1;

                prefs.edit()
                        .putInt(safeNumber + KEY_CALL_COUNT, count)
                        .putLong(safeNumber + KEY_LAST_DURATION, durationSeconds)
                        .remove(safeNumber + KEY_LAST_START)
                        .apply();

                Log.d(TAG, "⏱ Call duration saved: " + durationSeconds + "s");
                Log.d(TAG, "🔁 Updated call count: " + count);
            }

            // -------- REMOVE OVERLAY SAFELY --------
            context.stopService(
                    new Intent(context, OverlayWarningService.class)
            );
        }
    }

    // ================= NUMBER NORMALIZATION =================
    private String normalizeNumber(String number) {

        if (TextUtils.isEmpty(number)) return "";

        String cleaned = number.replaceAll("[^0-9+]", "");

        // Keep last 10 digits for Indian numbers
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(cleaned.length() - 10);
        }

        return cleaned;
    }
}
