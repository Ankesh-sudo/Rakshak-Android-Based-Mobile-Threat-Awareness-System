package com.rakshak.security.core;

import android.content.Context;
import android.content.SharedPreferences;

public class SecurityRiskStorage {

    private static final String PREF_NAME = "rakshak_security";
    private static final String KEY_SMS_RISK = "sms_risk";

    public static void setLastSmsRisk(Context context, int score) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit().putInt(KEY_SMS_RISK, score).apply();
    }

    public static int getLastSmsRisk(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getInt(KEY_SMS_RISK, 0);
    }
}
