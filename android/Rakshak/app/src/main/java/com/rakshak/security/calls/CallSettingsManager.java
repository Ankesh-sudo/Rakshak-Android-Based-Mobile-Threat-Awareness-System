package com.rakshak.security.calls;

import android.content.Context;
import android.content.SharedPreferences;

public class CallSettingsManager {

    private static final String PREF_NAME = "rakshak_call_settings";
    private static final String KEY_MODE = "protection_mode";

    private final SharedPreferences prefs;

    public CallSettingsManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // =====================================
    // Save Protection Mode
    // =====================================
    public void setMode(CallProtectionMode mode) {

        if (mode == null) return;

        prefs.edit()
                .putString(KEY_MODE, mode.name())
                .apply();
    }

    // =====================================
    // Get Protection Mode
    // =====================================
    public CallProtectionMode getMode() {

        String savedValue = prefs.getString(KEY_MODE, null);

        if (savedValue == null) {
            return CallProtectionMode.ALL_CALLS_ALLOWED;
        }

        try {
            return CallProtectionMode.valueOf(savedValue);
        } catch (IllegalArgumentException e) {
            return CallProtectionMode.ALL_CALLS_ALLOWED;
        }
    }

    // =====================================
    // Optional: Reset to Default
    // =====================================
    public void resetToDefault() {
        setMode(CallProtectionMode.ALL_CALLS_ALLOWED);
    }
}
