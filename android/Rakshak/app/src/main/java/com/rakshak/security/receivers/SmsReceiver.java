package com.rakshak.security.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.SmsMessage;
import android.util.Log;

import com.rakshak.security.core.RiskEngine;
import com.rakshak.security.core.RiskResult;
import com.rakshak.security.utils.NotificationUtil;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "Rakshak-SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null || intent.getExtras() == null) return;

        Object[] pdus = (Object[]) intent.getExtras().get("pdus");
        String format = intent.getExtras().getString("format");

        if (pdus == null || pdus.length == 0) return;

        for (Object pdu : pdus) {

            SmsMessage sms;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                } else {
                    sms = SmsMessage.createFromPdu((byte[]) pdu);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to parse SMS PDU", e);
                continue;
            }

            if (sms == null) continue;

            String sender = sms.getOriginatingAddress();
            String message = sms.getMessageBody();

            Log.d(TAG, "📩 SMS received from: " + sender);
            Log.d(TAG, "📝 Message: " + message);

            // ---------- STEP 2: RISK ANALYSIS ----------
            RiskResult result =
                    RiskEngine.analyzeIncomingSms(sender, message);

            Log.d(TAG, "🧠 Risk Score: " + result.score);
            Log.d(TAG, "🚦 Risk Level: " + result.level.name());

            // ---------- USER WARNING (NOTIFICATION) ----------
            if (result.level == RiskResult.RiskLevel.MEDIUM
                    || result.level == RiskResult.RiskLevel.HIGH) {

                NotificationUtil.showSmsWarning(
                        context,
                        sender,
                        result
                );
            }
        }
    }
}
