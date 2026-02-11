package com.rakshak.security.calls;

import android.content.Context;
import android.content.SharedPreferences;

public class ProtectionModeManager {

    private static final String PREF_NAME = "rakshak_call_protection";
    private static final String KEY_MODE = "protection_mode";

    public enum ProtectionMode {
        BASIC,
        SMART,
        EXTREME
    }

    public static void setMode(Context context, ProtectionMode mode) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit().putString(KEY_MODE, mode.name()).apply();
    }

    public static ProtectionMode getMode(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String mode = prefs.getString(KEY_MODE,
                ProtectionMode.SMART.name());

        return ProtectionMode.valueOf(mode);
    }
}
