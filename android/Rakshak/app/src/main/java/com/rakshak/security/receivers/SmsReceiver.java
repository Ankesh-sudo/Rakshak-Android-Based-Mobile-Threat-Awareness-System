package com.rakshak.security.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.rakshak.security.core.RiskEngine;
import com.rakshak.security.core.RiskResult;
import com.rakshak.security.core.database.RiskDatabase;
import com.rakshak.security.core.database.RiskEntity;
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

            // 🔥 Analyze SMS
            RiskResult result =
                    RiskEngine.analyzeIncomingSms(sender, body);

            // 🔥 Save to Room Database (background thread)
            new Thread(() -> {

                RiskEntity entity = new RiskEntity(
                        "SMS",
                        sender != null ? sender : "Unknown",
                        result.getScore(),
                        result.getRiskLevel().name(),
                        System.currentTimeMillis()
                );

                RiskDatabase.getInstance(context)
                        .riskDao()
                        .insert(entity);

            }).start();

            // 🔥 Show Notification if risk is Medium or High
            if (result.shouldWarnUser()) {

                NotificationUtil.showSmsWarning(
                        context,
                        sender,
                        body,
                        result
                );
            }
        }
    }
}
